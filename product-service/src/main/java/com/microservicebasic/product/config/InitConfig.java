package com.microservicebasic.product.config;

import com.microservicebasic.product.dto.ErrorDetail;
import com.microservicebasic.product.filter.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
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


    static class CommonResponseErrorHandler<T> extends DefaultResponseErrorHandler {

        private final HttpMessageConverterExtractor<T> jacksonMessageConverter;

        public CommonResponseErrorHandler(Class<T> responseType) {

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            this.jacksonMessageConverter = new HttpMessageConverterExtractor<>(responseType, messageConverters);
        }

    }

}
