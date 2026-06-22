package com.example.ms_order.dto;

import com.example.ms_order.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DTO que representa una orden de compra con enlaces HATEOAS")
public class OrderDto extends RepresentationModel<OrderDto> {

    @Schema(description = "Identificador único de la orden", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre de usuario del cliente", example = "juan.perez")
    private String username;

    @Schema(description = "Estado actual de la orden", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Estado del pago", example = "PENDING")
    private String paymentStatus;

    @Schema(description = "Monto total de la orden", example = "150.75")
    private Double totalAmount;

    @Schema(description = "Fecha de creación de la orden")
    private LocalDateTime createdAt;

    @Schema(description = "Lista de ítems de la orden")
    private List<OrderItemDto> items;
}