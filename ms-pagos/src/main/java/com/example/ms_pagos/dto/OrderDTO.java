package com.example.ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Datos de una orden")
public class OrderDTO {

    @Schema(description = "ID de la orden", example = "1")
    private Long orderId;

    @Schema(description = "ID del usuario", example = "1")
    private Long userId;

    @Schema(description = "Items de la orden")
    private List<OrderItemDTO> items;

    @Schema(description = "Monto total de la orden", example = "99.99")
    private Double totalAmount;

    @Schema(description = "Estado de la orden", example = "PENDING")
    private String status;
}