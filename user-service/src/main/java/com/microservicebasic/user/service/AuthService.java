package com.microservicebasic.user.service;

import com.microservicebasic.user.dto.authmanagement.GenerateTokenResponse;
import com.microservicebasic.user.dto.authmanagement.RevokeTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;

    @Value("${url.generateToken}")
    private String generateTokenUrl;

    @Value("${oauth2.clientId}")
    private String oauth2ClientId;

    @Value("${url.revokeToken}")
    private String revokeTokenUrl;

    @Value("${oauth2.clientSecret}")
    private String oauth2ClientSecret;

    public GenerateTokenResponse generateToken(String userId, String channel) {
        log.info("generate token start ");
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", oauth2ClientId);
        map.add("client_secret", oauth2ClientSecret);
        map.add("grant_type", "client_credentials");
        map.add("user_id", userId);
        map.add("channel", channel);

        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, httpHeaders);
        ResponseEntity<GenerateTokenResponse> response = restTemplate.postForEntity(generateTokenUrl,
                entity, GenerateTokenResponse.class);

        return response.getBody();
    }

    public RevokeTokenResponse revokeToken(HttpHeaders header) {
        var entity = new HttpEntity<String>(null, header);

        log.info("revokeToken url : {}", revokeTokenUrl);
        try {
            ResponseEntity<RevokeTokenResponse> response = restTemplate.postForEntity(revokeTokenUrl, entity, RevokeTokenResponse.class);
            log.info("revokeToken response : {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("error when logout : {}", e.getMessage());
            throw e;
        }
    }
}
