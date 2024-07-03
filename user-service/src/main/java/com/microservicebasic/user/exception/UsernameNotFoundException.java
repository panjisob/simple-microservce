package com.microservicebasic.user.exception;

public class UsernameNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 5033950323101652185L;

    public UsernameNotFoundException(String message) {
        super(message);
    }


}
