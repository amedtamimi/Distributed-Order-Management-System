package com.dev.order_service.service;

import com.dev.order_service.client.CustomerClient;
import com.dev.order_service.client.ProductClient;
import com.dev.order_service.dto.*;
import com.dev.order_service.entity.Order;
import com.dev.order_service.entity.OrderItem;
import com.dev.order_service.enums.OrderStatus;
import com.dev.order_service.exception.OrderNotFoundException;
import com.dev.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderValidationService orderValidationService;

    @Cacheable(value = "orders", key = "#id")
    public OrderDTO getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return convertToDTO(order);
    }

    public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderConfirmationDTO createOrder(OrderDTO orderDTO) {
        logger.info("Creating a new order for customer ID: {}", orderDTO.getCustomerId());

        logger.debug("Validating order DTO: {}", orderDTO);
        orderValidationService.validateOrder(orderDTO);

        Order order = convertToEntity(orderDTO);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        calculateTotalAmount(order);

        logger.info("Saving order to the database.");
        Order savedOrder = orderRepository.save(order);

        logger.info("Updating product stock for the order.");
        updateProductStock(savedOrder, true);

        OrderConfirmationDTO confirmation = createOrderConfirmation(savedOrder);
        logger.info("Order created successfully with confirmation number: {}", confirmation.getConfirmationNumber());

        return confirmation;
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Validate order cancellation
        orderValidationService.validateOrderCancellation(order);

        // Restore stock
        updateProductStock(order, false);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void updateProductStock(Order order, boolean isDeduct) {
        order.getItems().forEach(item -> {
            StockUpdateDTO stockUpdate = new StockUpdateDTO();
            stockUpdate.setProductId(item.getProductId());
            stockUpdate.setQuantity(item.getQuantity());

            if (isDeduct) {
                productClient.deductStock(item.getProductId(), stockUpdate);
            } else {
                productClient.restoreStock(item.getProductId(), stockUpdate);
            }
        });
    }

    private void calculateTotalAmount(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
    }

    private OrderConfirmationDTO createOrderConfirmation(Order order) {
        CustomerDTO customer = customerClient.getCustomer(order.getCustomerId());

        return OrderConfirmationDTO.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getItems().size())
                .confirmationNumber(generateConfirmationNumber())
                .build();
    }

    private String generateConfirmationNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomerId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setNotes(order.getNotes());

        // Convert order items
        dto.setItems(order.getItems().stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList()));

        // Get customer name from customer service
        try {
            CustomerDTO customer = customerClient.getCustomer(order.getCustomerId());
            dto.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        } catch (Exception e) {
            dto.setCustomerName("Customer info unavailable");
        }

        return dto;
    }

    private OrderItemDTO convertToItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());

        // Get product name from product service
        try {
            ProductDTO product = productClient.getProduct(item.getProductId());
            dto.setProductName(product.getName());
        } catch (Exception e) {
            dto.setProductName("Product info unavailable");
        }

        return dto;
    }

    private Order convertToEntity(OrderDTO dto) {
        Order order = new Order();
        order.setCustomerId(dto.getCustomerId());
        order.setNotes(dto.getNotes());

        // Convert order items
        List<OrderItem> items = dto.getItems().stream()
                .map(itemDTO -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemDTO.getProductId());
                    item.setQuantity(itemDTO.getQuantity());
                    item.setUnitPrice(itemDTO.getUnitPrice());
                    item.setOrder(order);
                    return item;
                })
                .collect(Collectors.toList());

        order.setItems(items);
        return order;
    }
}