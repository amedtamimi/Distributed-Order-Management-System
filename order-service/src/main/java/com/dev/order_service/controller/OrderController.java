package com.dev.order_service.controller;

import com.dev.order_service.dto.OrderDTO;
import com.dev.order_service.dto.OrderConfirmationDTO;
import com.dev.order_service.service.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @GetMapping("/{id}")
    @RateLimiter(name = "orderService")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/customer/{customerId}")
    @RateLimiter(name = "orderService")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/date-range")
    @RateLimiter(name = "orderService")
    public ResponseEntity<List<OrderDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(orderService.getOrdersByDateRange(startDate, endDate));
    }

    @PostMapping
    @RateLimiter(name = "orderService")
    public ResponseEntity<OrderConfirmationDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        logger.info("Entering createOrder method.");
        logger.debug("Received OrderDTO: {}", orderDTO);

        logger.info("Calling orderService.createOrder method.");
        OrderConfirmationDTO confirmation = orderService.createOrder(orderDTO);

        logger.info("Order created successfully. OrderConfirmationDTO: {}", confirmation);
        return new ResponseEntity<>(confirmation, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/cancel")
    @RateLimiter(name = "orderService")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}