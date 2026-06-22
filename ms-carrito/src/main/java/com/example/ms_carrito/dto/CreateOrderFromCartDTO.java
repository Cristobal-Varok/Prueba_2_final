package com.example.ms_carrito.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear una orden a partir del carrito")
public class CreateOrderFromCartDTO {

    @Schema(description = "ID del usuario", example = "1")
    private Long userId;

    @Schema(description = "Lista de items de la orden")
    private List<OrderItemDTO> items;

    @Schema(description = "Monto total de la orden", example = "59.97")
    private Double totalAmount;

    @Schema(description = "Dirección de envío", example = "Calle 123, Santiago")
    private String shippingAddress;

    @Schema(description = "Código de cupón aplicado", example = "VERANO2025")
    private String couponCode;
}