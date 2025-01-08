package com.dev.order_service.client;

import com.dev.order_service.client.fallback.ProductClientFallback;
import com.dev.order_service.dto.ProductDTO;
import com.dev.order_service.dto.StockUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "product-service",
        url = "${client.product-service.url}",
        fallback = ProductClientFallback.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProduct(@PathVariable("id") Long id);

    @PostMapping("/api/products/{id}/deduct-stock")
    void deductStock(@PathVariable("id") Long id, @RequestBody StockUpdateDTO stockUpdate);

    @PostMapping("/api/products/{id}/restore-stock")
    void restoreStock(@PathVariable("id") Long id, @RequestBody StockUpdateDTO stockUpdate);
}