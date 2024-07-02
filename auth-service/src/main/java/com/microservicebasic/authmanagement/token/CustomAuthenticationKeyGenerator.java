package com.microservicebasic.authmanagement.token;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;

public class CustomAuthenticationKeyGenerator implements AuthenticationKeyGenerator {

    private static final String CLIENT_ID = "client_id";

    private static final String SCOPE = "scope";

    private static final String USERNAME = "username";

    private static final String USER_ID = "user_id";

    private static final String DEVICE_ID = "device_id";
    private static final String QUICK_ACCESS = "quick_access";


    private String getUserId(OAuth2Authentication oAuth2Authentication) {
        Map<String, String> requestParameters = oAuth2Authentication.getOAuth2Request().getRequestParameters();
        return requestParameters.get(USER_ID);
    }

    private String getDeviceId(OAuth2Authentication oAuth2Authentication) {
        Map<String, String> requestParameters = oAuth2Authentication.getOAuth2Request().getRequestParameters();
        return requestParameters.get(DEVICE_ID);
    }

    private String isQuickAccess(OAuth2Authentication oAuth2Authentication) {
        return oAuth2Authentication.getOAuth2Request().getRequestParameters().get(QUICK_ACCESS);
    }

    @Override
    public String extractKey(OAuth2Authentication authentication) {
        Map<String, String> values = new LinkedHashMap<>();
        OAuth2Request authorizationRequest = authentication.getOAuth2Request();
        if (!authentication.isClientOnly()) {
            values.put(USERNAME, authentication.getName());
        }
        values.put(CLIENT_ID, authorizationRequest.getClientId());
        if (authorizationRequest.getScope() != null) {
            values.put(SCOPE, OAuth2Utils.formatParameterList(new TreeSet<>(authorizationRequest.getScope())));
        }
        String userId = getUserId(authentication);
        values.put(USER_ID, userId);
        String deviceId = this.getDeviceId(authentication);
        values.put(DEVICE_ID, deviceId);
        values.put(QUICK_ACCESS, this.isQuickAccess(authentication));
        return generateKey(values);
    }

    protected String generateKey(Map<String, String> values) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-512");
            byte[] bytes = digest.digest(values.toString().getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", e);
        }
    }
}
