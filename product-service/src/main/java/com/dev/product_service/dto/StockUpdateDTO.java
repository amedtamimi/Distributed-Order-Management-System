package com.dev.product_service.dto;

import lombok.Data;

@Data
public class StockUpdateDTO {
    private Long productId;
    private Integer quantity;
}