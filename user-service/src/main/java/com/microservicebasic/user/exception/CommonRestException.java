package com.microservicebasic.user.exception;

import com.microservicebasic.user.dto.ErrorDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

public class CommonRestException extends RestClientException {

    private static final long serialVersionUID = 525587716280227799L;

    protected transient ResponseEntity<ErrorDetail> responseEntity;

    public CommonRestException(String message) {
        super(message);
    }

    public ResponseEntity<ErrorDetail> getResponseEntity() {
        return this.responseEntity;
    }

    public void setResponseEntity(ResponseEntity<ErrorDetail> responseEntity) {
        this.responseEntity = responseEntity;
    }

}
