package com.microservicebasic.user.exception;

public class UserInActiveException extends RuntimeException {

    private static final long serialVersionUID = -4006097031687806505L;

    public UserInActiveException(String message) {
        super(message);
    }


}
