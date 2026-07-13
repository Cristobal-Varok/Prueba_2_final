package com.example.ms_envios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "DTO de una orden obtenida desde ms-order")
public class OrderDTO {

    @Schema(description = "ID de la orden", example = "1")
    private Long id;

    @Schema(description = "ID del cliente", example = "101")
    private Long clientId;

    @Schema(description = "Estado de la orden", example = "PAID", allowableValues = {"PENDING", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"})
    private String status;

    @Schema(description = "Estado del pago", example = "PAID", allowableValues = {"PENDING", "PAID", "PAYMENT_FAILED"})
    private String paymentStatus;

    @Schema(description = "Dirección de envío de la orden", example = "Av. Siempre Viva 123")
    private String shippingAddress;

    @Schema(description = "Monto total de la orden", example = "36.0")
    private Double totalAmount;

    @Schema(description = "Fecha de creación de la orden", example = "2026-06-08T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Items de la orden")
    private List<OrderItemDTO> items;
}

@Data
@Schema(description = "Item de una orden")
class OrderItemDTO {

    @Schema(description = "ID del producto", example = "3")
    private Long productId;

    @Schema(description = "Cantidad", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario", example = "18.0")
    private Double price;
}