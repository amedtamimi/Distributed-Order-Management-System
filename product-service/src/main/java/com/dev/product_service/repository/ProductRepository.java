package com.dev.product_service.repository;


import com.dev.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Find by SKU with pessimistic lock for stock updates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Optional<Product> findBySkuWithLock(@Param("sku") String sku);

    // Find products with stock below threshold
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);

    // Find products by price range
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find products with stock greater than zero
    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    // Find by exact SKU
    Optional<Product> findBySku(String sku);

    // Check if SKU exists
    boolean existsBySku(String sku);

    // Find products by stock quantity less than or equal to
    List<Product> findByStockQuantityLessThanEqual(Integer quantity);

    // Custom query to find products and order by stock quantity
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 ORDER BY p.stockQuantity ASC")
    List<Product> findAvailableProductsOrderByStock();

    // Native query example for complex stock analysis
    @Query(value = "SELECT * FROM products p WHERE p.stock_quantity > 0 AND p.price <= :maxPrice",
            nativeQuery = true)
    List<Product> findAvailableProductsWithinPrice(@Param("maxPrice") BigDecimal maxPrice);

    // Update stock quantity directly
    @Query("UPDATE Product p SET p.stockQuantity = :quantity WHERE p.id = :id")
    void updateStockQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    // Count products with zero stock
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = 0")
    long countOutOfStockProducts();
}