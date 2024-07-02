package com.microservicebasic.authmanagement.config;

import com.microservicebasic.authmanagement.dto.ErrorDetail;
import com.microservicebasic.authmanagement.exception.CommonRestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class InitConfig {

    @Value(value = "${rest.client.connect.timeout:2}")
    private long restClientConnectTimeout;

    @Value(value = "${rest.client.read.timeout:5}")
    private long restClientReadTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(requestFactory);
        var restTemplate = restTemplateBuilder
                .setReadTimeout(Duration.ofSeconds(restClientReadTimeout))
                .setConnectTimeout(Duration.ofSeconds(restClientConnectTimeout))
                .build();
        restTemplate.setRequestFactory(factory);
        restTemplate.setErrorHandler(new CommonResponseErrorHandler<>(ErrorDetail.class));
        return restTemplate;
    }

    static class CommonResponseErrorHandler<T> extends DefaultResponseErrorHandler {

        private final HttpMessageConverterExtractor<T> jacksonMessageConverter;

        public CommonResponseErrorHandler(Class<T> responseType) {

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            this.jacksonMessageConverter = new HttpMessageConverterExtractor<>(responseType, messageConverters);
        }

        @SuppressWarnings("unchecked")
		@Override
        public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {

            ResponseEntity<T> responseEntity = new ResponseEntity<>(this.jacksonMessageConverter.extractData(response),
                    response.getHeaders(), response.getStatusCode());

            var commonRestException = new CommonRestException(response.getRawStatusCode() + "-" + response
                    .getStatusCode().getReasonPhrase());

            if (null == responseEntity.getBody()) {
                commonRestException.setResponseEntity(ResponseEntity.status(response.getStatusCode())
                        .body(ErrorDetail.builder().build()));
            } else {
                commonRestException.setResponseEntity((ResponseEntity<ErrorDetail>) responseEntity);
            }

            throw commonRestException;
        }

    }

}
