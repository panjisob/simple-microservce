package com.microservicebasic.authmanagement.controller;

import com.microservicebasic.authmanagement.dto.RevokeTokenResponse;
import com.microservicebasic.authmanagement.dto.RevokeTokenUserRequest;
import com.microservicebasic.authmanagement.dto.ValidateTokenResponse;
import com.microservicebasic.authmanagement.service.AuthorizationManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationManagementController {

    @Autowired
    private AuthorizationManagementService authorizationManagementService;

    @PostMapping("/validatetoken")
    public ValidateTokenResponse validateToken(OAuth2Authentication oAuth2Authentication) {
        return authorizationManagementService.validateToken(oAuth2Authentication);
    }

    @PostMapping("/revoketoken")
    public RevokeTokenResponse revokeToken(OAuth2Authentication oAuth2Authentication) {
        return authorizationManagementService.revokeToken(oAuth2Authentication);
    }

    @PostMapping("/revoketokenuser")
    public RevokeTokenResponse revokeTokenUser(@RequestBody RevokeTokenUserRequest request){
        return authorizationManagementService.revokeTokenUser(request);
    }
    
}

