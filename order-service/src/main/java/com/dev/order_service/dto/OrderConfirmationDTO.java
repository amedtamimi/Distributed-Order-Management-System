package com.dev.order_service.dto;

import com.dev.order_service.enums.OrderStatus;
import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderConfirmationDTO {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private int totalItems;
    private String confirmationNumber;
}