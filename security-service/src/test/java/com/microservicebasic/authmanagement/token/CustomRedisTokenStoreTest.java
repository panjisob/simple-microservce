package com.microservicebasic.authmanagement.token;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStoreSerializationStrategy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CustomRedisTokenStore.class)
public class CustomRedisTokenStoreTest {

    @Autowired
    private CustomRedisTokenStore customRedisTokenStore;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    @Qualifier("redisOperator1")
    private HashOperations<String, Object, Object> redisOperator1;

    private OAuth2Authentication authenticationRequest(){
        Map<String, String> requestParameters = new HashMap<>();
        Collection<? extends GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        boolean approved = true;
        Set<String> scope = null;
        Set<String> resourceIds = null;
        String redirectUri = "http://google.com";
        Set<String> responseTypes = new HashSet<>();
        Map<String, Serializable> extensionProperties = new HashMap<>();
        OAuth2Request storedRequest = new OAuth2Request(requestParameters, "1234", authorities, approved, scope,
                resourceIds, redirectUri, responseTypes, extensionProperties);
        Object principal = new HashMap<>();
        Object credentials = new HashMap<>();
        TestingAuthenticationToken userAuth = new TestingAuthenticationToken(principal, credentials);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuYXV0aDAuY29tLyIsImF1ZCI6Imh0dHBzOi8vYXBpLmV4YW1wbGUuY29tL2NhbGFuZGFyL3YxLyIsInN1YiI6InVzcl8xMjMiLCJpYXQiOjE0NTg3ODU3OTYsImV4cCI6MTQ1ODg3MjE5Nn0.CA7eaHjIHz5NxeIJoFK9krqaeZrPLwmMmgI_XiQiIkQ");
        OAuth2AuthenticationDetails oAuth2AuthenticationDetails = new OAuth2AuthenticationDetails(mockHttpServletRequest);
        Authentication userAuthentication = userAuth;
        OAuth2Authentication authentication = new OAuth2Authentication(storedRequest, userAuthentication);
        authentication.setDetails(oAuth2AuthenticationDetails);
        return authentication;
    }

    private static final OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken() {
        @Override
        public Map<String, Object> getAdditionalInformation() {
            return null;
        }

        @Override
        public Set<String> getScope() {
            return null;
        }

        @Override
        public OAuth2RefreshToken getRefreshToken() {
            return null;
        }

        @Override
        public String getTokenType() {
            return null;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public Date getExpiration() {
            return null;
        }

        @Override
        public int getExpiresIn() {
            return 0;
        }

        @Override
        public String getValue() {
            return "TOKEN";
        }
    };

    private static final OAuth2RefreshToken OAuth2RefreshToken = new OAuth2RefreshToken() {
        @Override
        public String getValue() {
            return null;
        }
    };

    @Test(expected = NullPointerException.class)
    public void getAccessToken(){
        customRedisTokenStore.getAccessToken(authenticationRequest());
    }

    @Test(expected = NullPointerException.class)
    public void readAuthentication(){

        customRedisTokenStore.readAuthentication(oAuth2AccessToken);
    }

    @Test(expected = NullPointerException.class)
    public void readAuthenticationForRefreshToken(){
        customRedisTokenStore.readAuthenticationForRefreshToken(OAuth2RefreshToken);
    }

    @Test(expected = Exception.class)
    public void storeAccessToken(){
        customRedisTokenStore.storeAccessToken(oAuth2AccessToken, authenticationRequest());
    }

    @Test(expected = Exception.class)
    public void findTokensByClientId(){
        customRedisTokenStore.findTokensByClientId("TOKEN");
    }

    @Test(expected = Exception.class)
    public void findTokensByClientIdAndUserName(){
        customRedisTokenStore.findTokensByClientIdAndUserName("","");
    }

    @Test(expected = Exception.class)
    public void removeAccessTokenUsingRefreshToken(){
        customRedisTokenStore.removeAccessTokenUsingRefreshToken(null);
    }

    @Test(expected = Exception.class)
    public void removeRefreshToken(){
        customRedisTokenStore.removeRefreshToken("TOKEN");
    }

    @Test(expected = Exception.class)
    public void readRefreshToken(){
        customRedisTokenStore.readRefreshToken("TOKEN");
    }

    @Test(expected = Exception.class)
    public void readAccessToken(){
        customRedisTokenStore.readAccessToken("TOKEN");
    }

    @Test(expected = Exception.class)
    public void removeAccessToken(){
        customRedisTokenStore.removeAccessToken("TOKEN");
    }

    @Test(expected = Exception.class)
    public void storeRefreshToken(){
        customRedisTokenStore.storeRefreshToken(null, authenticationRequest());
    }

    @Test
    public void setAuthenticationKeyGenerator_success() {
        var auth = mock(AuthenticationKeyGenerator.class);
        customRedisTokenStore.setAuthenticationKeyGenerator(auth);
    }

    @Test
    public void setSerializationStrategy_success() {
        var redis = mock(RedisTokenStoreSerializationStrategy.class);
        customRedisTokenStore.setSerializationStrategy(redis);
    }

    @Test
    public void setPrefix_success() {
        customRedisTokenStore.setPrefix("tes");
    }

    @Test
    public void getAccessToken_success() {
        var oAuthAToken = mock(OAuth2Authentication.class);
        var oAuthReq = mock(OAuth2Request.class);
        var redisCon = mock(RedisConnection.class);
        when(oAuthAToken.getOAuth2Request()).thenReturn(oAuthReq);
        when(redisConnectionFactory.getConnection()).thenReturn(redisCon);
        customRedisTokenStore.getAccessToken(oAuthAToken);
    }
}
