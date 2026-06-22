package com.example.ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Item de una orden")
public class OrderItemDTO {

    @Schema(description = "ID del producto", example = "1")
    private Long productId;

    @Schema(description = "Cantidad del producto", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario", example = "14.99")
    private Double unitPrice;

    @Schema(description = "Subtotal del item", example = "29.98")
    private Double subtotal;
}