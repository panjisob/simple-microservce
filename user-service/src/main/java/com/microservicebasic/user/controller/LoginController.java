package com.microservicebasic.user.controller;

import com.microservicebasic.user.dto.login.LoginRequest;
import com.microservicebasic.user.dto.login.LoginResponse;
import com.microservicebasic.user.service.LoginService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) {
        return loginService.login(loginRequest, servletRequest, servletResponse);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest servletRequest) {
        loginService.revokeToken(servletRequest);
    }
}
