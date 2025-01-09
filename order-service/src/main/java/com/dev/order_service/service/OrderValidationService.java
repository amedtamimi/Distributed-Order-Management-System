package com.dev.order_service.service;

import com.dev.order_service.client.CustomerClient;
import com.dev.order_service.client.ProductClient;
import com.dev.order_service.dto.CustomerDTO;
import com.dev.order_service.dto.OrderDTO;
import com.dev.order_service.dto.ProductDTO;
import com.dev.order_service.entity.Order;
import com.dev.order_service.enums.OrderStatus;
import com.dev.order_service.exception.OrderValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderValidationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderValidationService.class);

    private final CustomerClient customerClient;
    private final ProductClient productClient;

    public void validateOrder(OrderDTO orderDTO) {
        logger.info("Starting validation for order with customer ID: {}", orderDTO.getCustomerId());
        validateCustomer(orderDTO.getCustomerId());
        validateOrderItems(orderDTO);
        validateTotalAmount(orderDTO);
        logger.info("Order validation completed successfully for customer ID: {}", orderDTO.getCustomerId());
    }

    public void validateOrderCancellation(Order order) {
        logger.info("Validating order cancellation for order ID: {}", order.getId());
        if (order.getStatus() != OrderStatus.PENDING) {
            logger.error("Order cancellation failed. Order ID: {} is not in PENDING status", order.getId());
            throw new OrderValidationException("Only PENDING orders can be cancelled");
        }
        logger.info("Order cancellation validation passed for order ID: {}", order.getId());
    }

    private void validateCustomer(Long customerId) {
        logger.info("Validating customer with ID: {}", customerId);
        try {
            CustomerDTO customer = customerClient.getCustomer(customerId);
            if (!customer.getActive()) {
                logger.error("Customer with ID: {} is not active", customerId);
                throw new OrderValidationException("Customer account is not active");
            }
            logger.info("Customer with ID: {} is active", customerId);
        } catch (Exception e) {
            logger.error("Failed to validate customer with ID: {}. Error: {}", customerId, e.getMessage(), e);
            throw new OrderValidationException("Failed to validate customer: " + e.getMessage());
        }
    }

    private void validateOrderItems(OrderDTO orderDTO) {
        logger.info("Validating items for order with customer ID: {}", orderDTO.getCustomerId());
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            logger.error("Order validation failed: No items in the order");
            throw new OrderValidationException("Order must contain at least one item");
        }

        orderDTO.getItems().forEach(item -> {
            logger.debug("Validating item with product ID: {}", item.getProductId());

            if (item.getQuantity() <= 0) {
                logger.error("Item validation failed: Quantity for product ID {} is not greater than zero", item.getProductId());
                throw new OrderValidationException("Item quantity must be greater than zero");
            }

            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Item validation failed: Unit price for product ID {} is not greater than zero", item.getProductId());
                throw new OrderValidationException("Item unit price must be greater than zero");
            }

            try {
                ProductDTO product = productClient.getProduct(item.getProductId());
                if (product.getStockQuantity() < item.getQuantity()) {
                    logger.error("Item validation failed: Insufficient stock for product {}. Available: {}, Requested: {}",
                            product.getName(), product.getStockQuantity(), item.getQuantity());
                    throw new OrderValidationException(
                            String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                                    product.getName(),
                                    product.getStockQuantity(),
                                    item.getQuantity())
                    );
                }
                logger.info("Item validation passed for product ID: {}", item.getProductId());
            } catch (Exception e) {
                logger.error("Failed to validate product with ID: {}. Error: {}", item.getProductId(), e.getMessage(), e);
                throw new OrderValidationException("Failed to validate product: " + e.getMessage());
            }
        });
    }

    private void validateTotalAmount(OrderDTO orderDTO) {
        logger.info("Validating total amount for order with customer ID: {}", orderDTO.getCustomerId());
        BigDecimal calculatedTotal = orderDTO.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (orderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            logger.error("Order validation failed: Total amount mismatch. Calculated: {}, Provided: {}",
                    calculatedTotal, orderDTO.getTotalAmount());
            throw new OrderValidationException("Order total amount does not match sum of items");
        }
        logger.info("Total amount validation passed for order with customer ID: {}", orderDTO.getCustomerId());
    }
}
