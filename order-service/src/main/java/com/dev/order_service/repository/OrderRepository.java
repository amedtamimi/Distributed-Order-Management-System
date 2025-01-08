package com.dev.order_service.repository;

import com.dev.order_service.entity.Order;
import com.dev.order_service.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    // Find orders between dates
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find orders by customer and status
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    // Find orders containing specific product
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.productId = :productId")
    List<Order> findByProductId(@Param("productId") Long productId);

    // Count orders by status
    long countByStatus(OrderStatus status);

    // Check if customer has any orders
    boolean existsByCustomerId(Long customerId);
}