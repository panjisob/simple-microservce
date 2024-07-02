package com.microservicebasic.authmanagement.service;

import com.microservicebasic.authmanagement.constant.AuthorizationConstant;
import com.microservicebasic.authmanagement.dto.RevokeTokenResponse;
import com.microservicebasic.authmanagement.dto.RevokeTokenUserRequest;
import com.microservicebasic.authmanagement.dto.ValidateTokenResponse;
import com.microservicebasic.authmanagement.token.CustomRedisTokenStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class AuthorizationManagementService {

    @Autowired
    private CustomRedisTokenStore tokenStore;

    @Autowired
    @Qualifier("redisOperator1")
    private HashOperations<String, Object, Object> redisOperator1;

    public ValidateTokenResponse validateToken(OAuth2Authentication oAuth2Authentication) {
        log.info("ValidateToken - start");
        try {
            Map<String, String> requestParameters = oAuth2Authentication.getOAuth2Request().getRequestParameters();
            String userId = requestParameters.get("user_id");
            String channel = requestParameters.get("channel");
            String ip = requestParameters.get("ip");
            String userAgent = requestParameters.get("user_agent");
            var oAuth2AuthenticationDetails = (OAuth2AuthenticationDetails) oAuth2Authentication.getDetails();
            String tokenValue = oAuth2AuthenticationDetails.getTokenValue();

            log.info("ValidateToken - get&set oauth access timeout");
            int oauth2AccessTokenTimeoutSec;
            if (AuthorizationConstant.CHANNEL_PORTAL.equals(channel)) {
                oauth2AccessTokenTimeoutSec = 900;
            } else {
                oauth2AccessTokenTimeoutSec = 300;
            }

            var defaultOAuth2AccessToken = new DefaultOAuth2AccessToken(tokenValue);
            defaultOAuth2AccessToken.setExpiration(new Date(System.currentTimeMillis() + (oauth2AccessTokenTimeoutSec * 1000L)));
            defaultOAuth2AccessToken.setScope(oAuth2Authentication.getOAuth2Request().getScope());

            log.info("ValidateToken - store access token");
            tokenStore.storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);

            var expiresIn = defaultOAuth2AccessToken.getExpiresIn();
            
            log.info("ValidateToken - finish");
            
            return ValidateTokenResponse.builder()
                    .userId(userId)
                    .channel(channel)
                    .tokenValue(tokenValue)
                    .expiresIn(String.valueOf(expiresIn))
                    .ip(ip)
                    .userAgent(userAgent)
                    .build();
            
        } catch (Exception e) {
            log.error("error validateToken :", e);
            throw e;
        }
    }

    /**
     * Remove token from RedisTokenStore
     * @param oAuth2Authentication authorization bearer token
     */
    public RevokeTokenResponse revokeToken(OAuth2Authentication oAuth2Authentication) {
    	log.info("RevokeToken - start");
        try {
            Map<String, String> requestParameters = oAuth2Authentication.getOAuth2Request().getRequestParameters();
            String userId = requestParameters.get("user_id");
            var oAuth2AuthenticationDetails = (OAuth2AuthenticationDetails) oAuth2Authentication.getDetails();
            
            tokenStore.removeAccessToken(oAuth2AuthenticationDetails.getTokenValue());
            oAuth2Authentication.eraseCredentials();

            log.info("RevokeToken - finish");
            return RevokeTokenResponse.builder()
                    .userId(userId)
                    .build();
        } catch (Exception e) {
            log.error("error revokeToken : ", e);
//            throw e;
            return null;
        }
    }

    public RevokeTokenResponse revokeTokenUser(RevokeTokenUserRequest revokeTokenUserRequest){
    	log.info("RevokeTokenUser - start");
        try{
            String userId = revokeTokenUserRequest.getUserId().toString();
            String channel = revokeTokenUserRequest.getChannel();
            tokenStore.removeAccessToken(revokeTokenUserRequest.getTokenValue());

            log.info("RevokeTokenUser - finish");
            return RevokeTokenResponse.builder()
                    .channel(channel)
                    .userId(userId)
                    .build();

        }catch (Exception e){
            log.error("error revokeToken user: ", e);
            throw e;
        }
    }

}
