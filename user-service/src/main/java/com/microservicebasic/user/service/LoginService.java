package com.microservicebasic.user.service;

import com.microservicebasic.user.dto.authmanagement.RevokeTokenResponse;
import com.microservicebasic.user.dto.login.LoginRequest;
import com.microservicebasic.user.dto.login.LoginResponse;
import com.microservicebasic.user.exception.IncorrectPassword;
import com.microservicebasic.user.exception.UserInActiveException;
import com.microservicebasic.user.exception.UsernameNotFoundException;
import com.microservicebasic.user.model.UserAuth;
import com.microservicebasic.user.model.UserProfile;
import com.microservicebasic.user.repository.UserAuthRepository;
import com.microservicebasic.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserAuthRepository userAuthRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthService authService;

    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        var activity = "SUCCESS";
        try {
            var optionalResult = userAuthRepository.findByUsername(request.getUsername());
            if (optionalResult.isEmpty()) {
                throw new UsernameNotFoundException("username not found");
            }
            var userAuth = optionalResult.get();

            if (Objects.equals(userAuth.getStatus(), UserAuth.Status.INACTIVE.name())) {
                throw new UserInActiveException("user inactive");
            }
            validateEncryptedPassword(request.getPassword(), userAuth);
            var optionalUserProfile = userProfileRepository.findById(userAuth.getUserId());
            var userProfile = optionalUserProfile.get();
            if (optionalUserProfile.isEmpty()) {
                throw new UserInActiveException("user inactive");
            }
            var generateTokenResponse = authService.generateToken(String.valueOf(userAuth.getUserId()), "USER_APP");
            servletResponse.setHeader("access_token", generateTokenResponse.getAccessToken());
            servletResponse.setHeader("expire", generateTokenResponse.getExpiresIn());

            return LoginResponse.builder()
                    .phone(userProfile.getPhone())
                    .address(userProfile.getAddress())
                    .email(userProfile.getEmail())
                    .firstName(userProfile.getFirstName())
                    .lastName(userProfile.getLastName())
                    .build();
        } catch (Exception e) {
            log.error("error login {}", e.getMessage());
            activity = "FAILED";
            throw e;
        } finally {
            //todo sand data activity via jms
            log.info("activity {} in ip {}", activity,servletRequest.getRemoteAddr());
        }

    }

    private void validateEncryptedPassword(String password, UserAuth userAuth) {
        //sample error
        if (!password.equals(userAuth.getPassword())) {
            throw new IncorrectPassword("incorrect password");
        }

        //TODO validate Encrypted Password
    }





    public void revokeToken(HttpServletRequest servletRequest) throws ResourceAccessException {
        var activity = "SUCCESS";
        try {
            var header = new HttpHeaders();
            Enumeration<String> headerNames = servletRequest.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String names = headerNames.nextElement();
                    header.add(names, servletRequest.getHeader(names));
                }
            }

            authService.revokeToken(header);
        } catch (Exception e) {
            log.error("error when logout {}", e.getMessage());
            activity = "FAILED";
            throw e;
        } finally {
            //todo sand data activity via jms
            log.info("activity {} in ip {}", activity,servletRequest.getRemoteAddr());
        }
    }
}
