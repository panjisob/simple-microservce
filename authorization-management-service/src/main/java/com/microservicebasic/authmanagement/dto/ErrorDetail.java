package com.microservicebasic.authmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail implements Serializable {

    private static final long serialVersionUID = 7859940117469970809L;

    private String errorCode;
    private String sourceSystem;
    private String message;
    private String transactionId;
    private String engMessage;
    private String idnMessage;
    private String activityRefCode;// used to called as trace id
    private String refNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int failedPinAttempts;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int settingFailedPinAttempts;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int failedPasswordAttempts;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int failedUserIdAttempts;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int failedSofTokenAttempts;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rsp;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rspdesc;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ref;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer statusCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String responseCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String responseMessage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private transient Object errors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
}
