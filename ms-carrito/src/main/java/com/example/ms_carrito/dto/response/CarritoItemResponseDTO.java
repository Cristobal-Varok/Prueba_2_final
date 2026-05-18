package com.example.ms_carrito.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarritoItemResponseDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
}