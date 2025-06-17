package com.microservicebasic.newsfeed.filter;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class LoggingWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingWebFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Wrap request untuk bisa membaca body berkali-kali
        ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return super.getBody()
                        .collectList()
                        .flatMapMany(dataBuffers -> {
                            DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                            DataBuffer joined = dataBufferFactory.join(dataBuffers);
                            byte[] bytes = new byte[joined.readableByteCount()];
                            joined.read(bytes);
                            DataBufferUtils.release(joined); // penting untuk hindari memory leak
                            String body = new String(bytes, StandardCharsets.UTF_8);
                            log.info("[REQUEST] {} {} body={}", request.getMethod(), request.getURI(), body);
                            return Flux.just(joined);
                        });
            }
        };

        // Wrap response untuk membaca dan log responsenya
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    return super.writeWith(fluxBody
                            .map(dataBuffer -> {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);
                                String responseBody = new String(content, StandardCharsets.UTF_8);
                                log.info("[RESPONSE] {} {} body={}", request.getMethod(), request.getURI(), responseBody);
                                return bufferFactory.wrap(content);
                            }));
                }
                return super.writeWith(body); // jika bukan Flux
            }
        };

        // Buat exchange baru dengan request dan response yang didekorasi
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(decoratedRequest)
                .response(decoratedResponse)
                .build();

        // Inject trace ID juga
        Map<String, String> headers = exchange.getRequest().getHeaders().toSingleValueMap();
        return chain.filter(mutatedExchange)
                .contextWrite(context -> {
                    var traceId = "";
                    if (headers.containsKey("X-B3-TRACEID")) {
                        traceId = headers.get("X-B3-TRACEID");
                        MDC.put("X-B3-TraceId", traceId);
                    } else if (!exchange.getRequest().getURI().getPath().contains("/actuator")) {
                        log.warn("TRACE_ID not present in header: {}", exchange.getRequest().getURI());
                    }

                    Context contextTmp = context.put("X-B3-TraceId", traceId);
                    exchange.getAttributes().put("X-B3-TraceId", traceId);

                    return contextTmp;
                });
    }
}

