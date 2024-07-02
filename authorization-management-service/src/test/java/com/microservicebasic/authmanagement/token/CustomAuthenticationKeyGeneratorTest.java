package com.microservicebasic.authmanagement.token;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CustomAuthenticationKeyGenerator.class)
public class CustomAuthenticationKeyGeneratorTest {

    @Autowired
    private CustomAuthenticationKeyGenerator customAuthenticationKeyGenerator;

    @Before
    public void init(){

    }


    private OAuth2Authentication request(){
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("channel", "MB");
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

    @Test
    public void generateKey_success(){
        Map<String, String> stringMap = new HashMap<>();
        customAuthenticationKeyGenerator.generateKey(stringMap);
    }

    @Test
    public void extractKey_success(){
        customAuthenticationKeyGenerator.extractKey(request());
    }
}
