package com.dev.product_service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductNotFoundException(Long productId) {
        super(String.format("Product not found with id: %d", productId));
    }

    public ProductNotFoundException(String sku, String field) {
        super(String.format("Product not found with %s: %s", field, sku));
    }
}