package com.dev.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(Long orderId) {
        super(String.format("Order not found with id: %d", orderId));
    }
}
