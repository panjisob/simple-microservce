package com.microservicebasic.user.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final List<String> sensitiveFields = List.of("password","mpin","Authorization","Cookie");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = wrapRequest(request);
        ContentCachingResponseWrapper wrappedResponse = wrapResponse(response);
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequestAndResponse(wrappedRequest, wrappedResponse, duration);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        if (request.getRequestURI().contains("/actuator/health")) {
            return;
        }
        String requestPayload = formatPayload(request.getContentAsByteArray());
        String responsePayload = formatPayload(response.getContentAsByteArray());
        String headersAsString = Collections.list(request.getHeaderNames())
                .stream()
                .map(header -> {
                    String value = sensitiveFields.contains(header.toLowerCase()) ? "*****" : request.getHeader(header);
                    return header + ": " + value;
                })
                .collect(Collectors.joining(", "));

        String logMessage = String.format(
                """
                
                ================================ REQUEST & RESPONSE ================================
                Method & URI  : %s  %s
                Headers       : %s
                Request Body  : %s
                ------------------------------------------------------------------------------------
                Status        : %d %s
                Duration      : %d ms
                Response Body : %s
                ====================================================================================
                """,
                request.getMethod(),
                request.getRequestURI(),
                headersAsString,
                maskSensitiveData(requestPayload),
                response.getStatus(),
                HttpStatus.valueOf(response.getStatus()).getReasonPhrase(),
                duration,
                maskSensitiveData(responsePayload)

        );

        log.info(logMessage);
    }

    private String formatPayload(byte[] payloadBytes) {
        String payload = new String(payloadBytes, StandardCharsets.UTF_8).trim();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object json = objectMapper.readValue(payload, Object.class);
            return objectMapper.writeValueAsString(json); // Convert to minified JSON
        } catch (Exception e) {
            return payload; // Return as-is if not valid JSON
        }
    }

    private String maskSensitiveData(String payload) {
        if (payload == null || payload.isEmpty()) {
            return payload;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(payload);

            // Iterate over sensitive fields and mask them
            for (String field : sensitiveFields) {
                if (rootNode.has(field)) {
                    ((ObjectNode) rootNode).put(field, "*****"); // Fully mask sensitive data
                }
            }

            return rootNode.toString(); // Convert the modified tree back to JSON string
        } catch (Exception e) {
            return payload; // Return as-is if not valid JSON
        }
    }


    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        }
        return new ContentCachingRequestWrapper(request);
    }

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        }
        return new ContentCachingResponseWrapper(response);
    }
}


