package com.dev.order_service.dto;

import lombok.Data;

@Data
public class StockUpdateDTO {
    private Long productId;
    private Integer quantity;
}
