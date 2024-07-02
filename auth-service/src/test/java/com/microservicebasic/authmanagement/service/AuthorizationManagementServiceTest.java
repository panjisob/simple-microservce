package com.microservicebasic.authmanagement.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.microservicebasic.authmanagement.dto.RevokeTokenUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.microservicebasic.authmanagement.constant.AuthorizationConstant;
import com.microservicebasic.authmanagement.exception.AlreadyLoginException;
import com.microservicebasic.authmanagement.token.CustomRedisTokenStore;

@RunWith(SpringRunner.class)
@TestPropertySource(locations="classpath:application.properties")
@ContextConfiguration(classes = AuthorizationManagementService.class)
public class AuthorizationManagementServiceTest {

    @Autowired
    private AuthorizationManagementService authorizationManagementService;

    @MockBean
    private CustomRedisTokenStore tokenStore;

    public static final String AUTH_HEADER = "access_token";
    public static final String ACCESS_TOKEN = "tokenValue";
    public static final String USER_ID = "userId";

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean(name = "redisOperator1")
    @Qualifier("redisOperator1")
    private HashOperations<String, Object, Object> redisOperator;

    private static final OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken() {
        @Override
        public Map<String, Object> getAdditionalInformation() {
            Map<String, Object> data1 = new HashMap<>();
            data1.put("ini", new Object());
            return data1;
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
            return null;
        }
    };

    private OAuth2Authentication request(){
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("channel", AuthorizationConstant.CHANNEL_PORTAL);
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

    private OAuth2Authentication requestMb(){
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("channel", AuthorizationConstant.CHANNEL_MB);
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

    @Before
    public void init(){
        when(redisOperator.get(Mockito.any(), Mockito.any())).thenReturn("300");
    }

    @Test
    public void validateToken_success(){
        when(tokenStore.readAccessToken(ArgumentMatchers.any())).thenReturn(oAuth2AccessToken);
        authorizationManagementService.validateToken(request());
    }

    @Test(expected = Exception.class)
    public void validateToken_errorException() {
        when(tokenStore.readAccessToken(ArgumentMatchers.any())).thenReturn(oAuth2AccessToken);
        doThrow(new AlreadyLoginException("tes")).when(tokenStore).storeAccessToken(any(), any());
        authorizationManagementService.validateToken(requestMb());
    }

    @Test
    public void revokeToken_success(){
        when(tokenStore.readAccessToken(ArgumentMatchers.any())).thenReturn(oAuth2AccessToken);
        authorizationManagementService.revokeToken(request());
    }

    @Test(expected = Exception.class)
    public void revokeToken_errorException() {
        when(tokenStore.readAccessToken(ArgumentMatchers.any())).thenReturn(oAuth2AccessToken);
        doThrow(new AlreadyLoginException("tes")).when(tokenStore).removeAccessToken(anyString());
        authorizationManagementService.revokeToken(request());
    }

    @Test(expected = Exception.class)
    public void revokeTokenUser() {
        authorizationManagementService.revokeTokenUser(null);
    }

    @Test
    public void revokeTokenUser_success() {
        doNothing().when(tokenStore).removeAccessToken(anyString());
        var response = authorizationManagementService.revokeTokenUser(RevokeTokenUserRequest.builder()
                .userId(1L)
                .tokenValue("84541fa7-ef5a-453f-b6c0-5c988c1286ef")
                .channel("MB")
                .build());
        assertEquals("1", response.getUserId());
    }
}
