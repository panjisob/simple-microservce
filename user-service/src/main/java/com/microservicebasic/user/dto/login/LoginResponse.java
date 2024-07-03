package com.microservicebasic.user.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 5774418230288589608L;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;

}
