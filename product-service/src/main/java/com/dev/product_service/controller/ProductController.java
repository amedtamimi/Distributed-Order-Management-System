package com.dev.product_service.controller;


import com.dev.product_service.dto.ProductDTO;
import com.dev.product_service.dto.StockUpdateDTO;
import com.dev.product_service.service.ProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    @RateLimiter(name = "productService")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping
    @RateLimiter(name = "productService")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    @RateLimiter(name = "productService")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        return new ResponseEntity<>(productService.createProduct(productDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "productService")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    @RateLimiter(name = "productService")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deduct-stock")
    @RateLimiter(name = "productService")
    public ResponseEntity<Void> deductStock(@RequestBody StockUpdateDTO stockUpdate, @PathVariable String id) {
        productService.deductStock(stockUpdate);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/restore-stock")
    @RateLimiter(name = "productService")
    public ResponseEntity<Void> restoreStock(@RequestBody StockUpdateDTO stockUpdate, @PathVariable String id) {
        productService.restoreStock(stockUpdate);
        return ResponseEntity.ok().build();
    }
}