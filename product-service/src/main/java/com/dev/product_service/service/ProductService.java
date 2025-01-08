package com.dev.product_service.service;


import com.dev.product_service.dto.ProductDTO;
import com.dev.product_service.dto.StockUpdateDTO;
import com.dev.product_service.exception.ProductNotFoundException;
import com.dev.product_service.exception.StockUpdateException;
import com.dev.product_service.entity.Product;
import com.dev.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductDTO getProduct(Long id) {
        return convertToDTO(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id)));
    }

    @Cacheable(value = "products", unless = "#result.isEmpty()")
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "products", key = "#result.id")
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        return convertToDTO(productRepository.save(product));
    }

    @CachePut(value = "products", key = "#id")
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        updateProductFields(existingProduct, productDTO);
        return convertToDTO(productRepository.save(existingProduct));
    }

    @Transactional
    @Retryable(value = {StockUpdateException.class}, maxAttempts = 3)
    @CacheEvict(value = {"products", "product-stock"}, key = "#stockUpdate.productId")
    public void deductStock(StockUpdateDTO stockUpdate) {
        Product product = productRepository.findById(stockUpdate.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStockQuantity() < stockUpdate.getQuantity()) {
            throw new StockUpdateException("Insufficient stock");
        }

        product.setStockQuantity(product.getStockQuantity() - stockUpdate.getQuantity());
        productRepository.save(product);

        // Update stock cache
        String stockCacheKey = "product-stock::" + stockUpdate.getProductId();
        redisTemplate.opsForValue().set(stockCacheKey, product.getStockQuantity());
    }

    @Transactional
    @Retryable(value = {StockUpdateException.class}, maxAttempts = 3)
    @CacheEvict(value = {"products", "product-stock"}, key = "#stockUpdate.productId")
    public void restoreStock(StockUpdateDTO stockUpdate) {
        Product product = productRepository.findById(stockUpdate.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setStockQuantity(product.getStockQuantity() + stockUpdate.getQuantity());
        productRepository.save(product);

        // Update stock cache
        String stockCacheKey = "product-stock::" + stockUpdate.getProductId();
        redisTemplate.opsForValue().set(stockCacheKey, product.getStockQuantity());
    }

    @CacheEvict(value = {"products", "product-stock"}, key = "#id")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }


    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        return dto;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        return product;
    }

    private void updateProductFields(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
    }
}