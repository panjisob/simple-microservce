package com.microservicebasic.authmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeTokenUserRequest implements Serializable {
    private static final long serialVersionUID = 7927568594718313872L;

    private String tokenValue;
    private Long userId;
    private String channel;
}
