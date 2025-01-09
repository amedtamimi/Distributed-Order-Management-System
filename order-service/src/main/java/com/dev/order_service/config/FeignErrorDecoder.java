package com.dev.order_service.config;

import com.dev.order_service.exception.OrderNotFoundException;
import com.dev.order_service.exception.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error Response: {} when calling {}", response.status(), methodKey);

        try {
            // Read the response body if available
            if (response.body() != null) {
                String responseBody = new String(response.body().asInputStream().readAllBytes());
                log.error("Error Response Body: {}", responseBody);
            }
        } catch (Exception e) {
            log.error("Could not read response body", e);
        }

        switch (response.status()) {
            case 400:
                return new IllegalArgumentException("Bad Request");
            case 404:
                return new OrderNotFoundException("Resource not found");
            case 408:
                return new ServiceUnavailableException("Request timeout");
            case 429:
                return new ServiceUnavailableException("Too many requests");
            case 503:
                return new ServiceUnavailableException("Service unavailable");
            default:
                if (response.status() >= 500) {
                    return new ServiceUnavailableException("Service error: " + response.status());
                }
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
