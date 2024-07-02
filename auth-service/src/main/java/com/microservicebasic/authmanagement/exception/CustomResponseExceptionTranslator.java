package com.microservicebasic.authmanagement.exception;

import com.microservicebasic.authmanagement.constant.AuthorizationConstant;
import com.microservicebasic.authmanagement.dto.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;

@Slf4j
@SuppressWarnings("rawtypes")
public class CustomResponseExceptionTranslator implements WebResponseExceptionTranslator {

    @Override
    public ResponseEntity<ErrorDetail> translate(Exception e) throws Exception {
        if (e instanceof OAuth2Exception) {
            OAuth2Exception oAuth2Exception = (OAuth2Exception) e;
            ErrorDetail errorDetail = ErrorDetail.builder()
                    .errorCode(AuthorizationConstant.ALREADY_LOGIN_ERROR_CODE)
                    .sourceSystem("Authorization Management")
                    .message(oAuth2Exception.getMessage())
                    .activityRefCode(MDC.get("X-B3-TraceId"))
                    .build();
            return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);

        }
        log.info("return null");
        return null;
    }
}
