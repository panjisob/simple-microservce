package com.microservicebasic.user.exception;

import com.microservicebasic.user.dto.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order
@RestControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {CommonRestException.class})
    public ResponseEntity<ErrorDetail> commonRestException(CommonRestException e) {
        log.info("Exception is commonRestException, message : {}", e.getMessage());
        return new ResponseEntity<>(e.getResponseEntity().getBody(), e.getResponseEntity().getStatusCode());
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> exception(Exception e) {
        log.error("Exception is UNCAUGHT, details : ", e);
        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode("01")
                .sourceSystem("Authorization Management")
                .message(e.getMessage())
                .engMessage("Something went wrong")
                .idnMessage("Terjadi kesalahan")
                .activityRefCode(MDC.get("X-B3-TraceId"))
                .build();

        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

}
