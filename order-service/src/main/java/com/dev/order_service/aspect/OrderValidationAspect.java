package com.dev.order_service.aspect;

import com.dev.order_service.annotation.ValidateOrder;
import com.dev.order_service.client.CustomerClient;
import com.dev.order_service.client.ProductClient;
import com.dev.order_service.dto.CustomerDTO;
import com.dev.order_service.dto.OrderDTO;
import com.dev.order_service.dto.ProductDTO;
import com.dev.order_service.exception.OrderValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderValidationAspect {

    private final CustomerClient customerClient;
    private final ProductClient productClient;

    @Around("@annotation(validateOrder)")
    public Object validateOrder(ProceedingJoinPoint joinPoint, ValidateOrder validateOrder) throws Throwable {
        // Extract OrderDTO from method arguments
        OrderDTO orderDTO = extractOrderDTO(joinPoint.getArgs());
        if (orderDTO == null) {
            throw new OrderValidationException("Order data not found in method arguments");
        }

        // Validate customer if required
        if (validateOrder.validateCustomer()) {
            validateCustomer(orderDTO.getCustomerId());
        }

        // Validate products and stock if required
        if (validateOrder.validateProducts()) {
            validateProducts(orderDTO);
        }

        // Proceed with the method execution
        return joinPoint.proceed();
    }

    private OrderDTO extractOrderDTO(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof OrderDTO) {
                return (OrderDTO) arg;
            }
        }
        return null;
    }

    private void validateCustomer(Long customerId) {
        try {
            CustomerDTO customer = customerClient.getCustomer(customerId);
            if (!customer.getActive()) {
                throw new OrderValidationException("Customer account is not active");
            }
        } catch (Exception e) {
            if (e instanceof OrderValidationException) {
                throw e;
            }
            throw new OrderValidationException("Failed to validate customer: " + e.getMessage());
        }
    }

    private void validateProducts(OrderDTO orderDTO) {
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new OrderValidationException("Order must contain at least one item");
        }

        orderDTO.getItems().forEach(item -> {
            try {
                // Validate product existence and stock
                ProductDTO product = productClient.getProduct(item.getProductId());

                // Validate product stock if required
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new OrderValidationException(
                            String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                                    product.getName(),
                                    product.getStockQuantity(),
                                    item.getQuantity())
                    );
                }
            } catch (Exception e) {
                if (e instanceof OrderValidationException) {
                    throw e;
                }
                throw new OrderValidationException("Failed to validate product: " + e.getMessage());
            }
        });
    }
}