package com.microservicebasic.product.filter;

import com.microservicebasic.product.annotation.UserAuthenticate;
import com.microservicebasic.product.dto.authmanagement.ValidateTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.validateToken}")
    private String validateTokenUrl;

//    @Autowired
//    @Qualifier("redisOps0")
//    private ValueOperations<String, Object> redisOps0;
//
//    @Autowired
//    @Qualifier("redisTemplate0")
//    private RedisTemplate<String, Object> redisTemplate0;

    private static final String ACCESS_TOKEN = "Authorization";
    private static final String REDIS_KEY_REQUEST_ID = "req_id";
    private static final long EXPIRED_MILLISECONDS = 300L;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(ACCESS_TOKEN);

        try {
            if (!(handler instanceof HandlerMethod)) {
                log.info("Wrong type of HandlerInterceptor");
                return true;
            }

            HandlerMethod hm = (HandlerMethod) handler;

            var userAuthenticate = AnnotationUtils.findAnnotation(hm.getMethod(), UserAuthenticate.class);

            if (userAuthenticate == null) {
                return true;
            }


            token = token.replace("Bearer ", "");

            if (StringUtils.isEmpty(token)) log.info("token is empty");
            log.debug("Bearer token : {}", token);
            var validateTokenResponse = validateToken(token);

            if (null == validateTokenResponse || StringUtils.isEmpty(validateTokenResponse.getTokenValue())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "token response is null");
                return false;
            }


            request.setAttribute("userId", Long.parseLong(validateTokenResponse.getUserId()));
            response.setHeader("Access-Control-Expose-Headers", "access_token, expire");
            response.setHeader("access_token", validateTokenResponse.getTokenValue());
            response.setHeader("expire", validateTokenResponse.getExpiresIn());

            return true;
        } catch (Exception e) {
            log.error("Error : ", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bearer token is invalid");
            return false;
        }
    }

    public ValidateTokenResponse validateToken(String token) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(token);

        HttpEntity<Object> entity = new HttpEntity<>(null, httpHeaders);

        log.info("validateToken url : {}", validateTokenUrl);

        ResponseEntity<ValidateTokenResponse> response = restTemplate.postForEntity(validateTokenUrl,
                entity, ValidateTokenResponse.class);

        log.info("validateToken response : {}", response.getBody());

        return response.getBody();
    }

}
