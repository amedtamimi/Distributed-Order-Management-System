package com.dev.order_service.client.fallback;

import com.dev.order_service.client.ProductClient;
import com.dev.order_service.dto.ProductDTO;
import com.dev.order_service.dto.StockUpdateDTO;
import com.dev.order_service.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductDTO getProduct(Long id) {
        throw new ServiceUnavailableException("Product service is currently unavailable");
    }

    @Override
    public void deductStock(Long id, StockUpdateDTO stockUpdate) {
        throw new ServiceUnavailableException("Product service is currently unavailable - cannot deduct stock");
    }

    @Override
    public void restoreStock(Long id, StockUpdateDTO stockUpdate) {
        throw new ServiceUnavailableException("Product service is currently unavailable - cannot restore stock");
    }
}