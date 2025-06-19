package com.microservicebasic.authmanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.microservicebasic.authmanagement.dto.RevokeTokenResponse;
import com.microservicebasic.authmanagement.dto.ValidateTokenResponse;
import com.microservicebasic.authmanagement.service.AuthorizationManagementService;

@RunWith(SpringRunner.class)
@TestPropertySource(locations="classpath:application.properties")
@WebMvcTest(controllers = AuthorizationManagementController.class)
public class AuthorizationManagementControllerTest {

    @MockBean
    private AuthorizationManagementService authorizationManagementService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;


    @Before
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); }


    @Test
    public void validateToken_success() throws Exception {
        Mockito.doReturn(ValidateTokenResponse.builder().build())
                .when(authorizationManagementService).validateToken(any());
        this.mockMvc.perform(
                        post("/validatetoken")
                                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void revoketoken_success() throws Exception {
        Mockito.doReturn(RevokeTokenResponse.builder().build())
                .when(authorizationManagementService).revokeToken(any());
        this.mockMvc.perform(
                        post("/revoketoken")
                                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
    }

}
