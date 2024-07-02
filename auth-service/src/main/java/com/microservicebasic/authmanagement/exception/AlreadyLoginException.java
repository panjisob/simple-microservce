package com.microservicebasic.authmanagement.exception;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

public class AlreadyLoginException extends OAuth2Exception {

    private static final long serialVersionUID = -9145438757387103377L;

    public AlreadyLoginException(String message) {
        super(message);
    }
}
