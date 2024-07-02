package com.microservicebasic.product.config;

import com.microservicebasic.product.dto.ErrorDetail;
import com.microservicebasic.product.exception.CommonRestException;
import com.microservicebasic.product.filter.AuthenticationInterceptor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
@EnableAsync
public class InitConfig implements WebMvcConfigurer {

    private static final int QUEUE_CAPACITY = 50;
    private static final int KEEP_ALIVE_SECONDS = 30;

    @Value(value = "${rest.client.connect.timeout:2}")
    private long restClientConnectTimeout;

    @Value(value = "${rest.client.read.timeout:5}")
    private long restClientReadTimeout;

    @Value(value = "${enabled.swagger:true}")
    private boolean enabledSwagger;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        var factory = new BufferingClientHttpRequestFactory(requestFactory);
        var restTemplate = restTemplateBuilder
                .setReadTimeout(Duration.ofSeconds(restClientReadTimeout))
                .setConnectTimeout(Duration.ofSeconds(restClientConnectTimeout))
                .build();
        restTemplate.setRequestFactory(factory);
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }

//        interceptors.add(auditPayloadRestTemplateInterceptor());
        restTemplate.setInterceptors(interceptors);
        restTemplate.setErrorHandler(new CommonResponseErrorHandler<>(ErrorDetail.class));
        return restTemplate;
    }

    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor());
    }

    @Bean
    public Docket api() {
        var parameterBuilder = new ParameterBuilder();
        parameterBuilder.name("Authorization")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(true)
                .defaultValue("Bearer token")
                .build();
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(parameterBuilder.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(parameters)
                .enable(enabledSwagger);
    }

    static class CommonResponseErrorHandler<T> extends DefaultResponseErrorHandler {

        private final HttpMessageConverterExtractor<T> jacksonMessageConverter;

        public CommonResponseErrorHandler(Class<T> responseType) {

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            this.jacksonMessageConverter = new HttpMessageConverterExtractor<>(responseType, messageConverters);
        }

        
		@Override
        @SneakyThrows
        @SuppressWarnings("unchecked")
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
