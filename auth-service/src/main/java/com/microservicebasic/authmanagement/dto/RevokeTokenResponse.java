package com.microservicebasic.authmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeTokenResponse implements Serializable {

    private static final long serialVersionUID = -1237516319229101614L;

    private String userId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String channel;

}
