package com.dev.order_service.config;

import com.dev.order_service.exception.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                return new RuntimeException("Bad Request");
            case 404:
                return new RuntimeException("Resource not found");
            case 503:
                return new ServiceUnavailableException("Service is currently unavailable");
            default:
                if (response.status() >= 500) {
                    return new ServiceUnavailableException("Service error occurred");
                }
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}