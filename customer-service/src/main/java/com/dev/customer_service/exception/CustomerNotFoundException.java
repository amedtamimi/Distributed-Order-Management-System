package com.dev.customer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(Long customerId) {
        super(String.format("Customer not found with id: %d", customerId));
    }

    public CustomerNotFoundException(String email, String field) {
        super(String.format("Customer not found with %s: %s", field, email));
    }
}