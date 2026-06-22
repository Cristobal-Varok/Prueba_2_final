package com.example.ms_pagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Datos del carrito")
public class CartDTO {

    @Schema(description = "ID del carrito", example = "1")
    private Long cartId;

    @Schema(description = "ID del usuario", example = "1")
    private Long userId;

    @Schema(description = "Items del carrito")
    private List<CartItemDTO> items;

    @Schema(description = "Total del carrito", example = "99.99")
    private Double total;
}