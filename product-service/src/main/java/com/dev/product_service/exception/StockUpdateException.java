package com.dev.product_service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockUpdateException extends RuntimeException {

    public StockUpdateException(String message) {
        super(message);
    }

    public StockUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public StockUpdateException(Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product id: %d. Requested: %d, Available: %d",
                productId, requestedQuantity, availableQuantity));
    }

    public StockUpdateException(String sku, Integer requestedQuantity) {
        super(String.format("Cannot update stock for product with SKU: %s. Invalid quantity: %d",
                sku, requestedQuantity));
    }
}