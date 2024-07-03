package com.microservicebasic.user.exception;

public class IncorrectPassword extends RuntimeException {

    private static final long serialVersionUID = -4006097031687806505L;

    public IncorrectPassword(String message) {
        super(message);
    }


}
