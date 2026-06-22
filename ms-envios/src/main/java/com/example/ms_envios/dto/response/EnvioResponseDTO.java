package com.example.ms_envios.dto.response;

import com.example.ms_envios.model.EnviosStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "DTO de respuesta con datos del envío")
public class EnvioResponseDTO extends RepresentationModel<EnvioResponseDTO> {

    @Schema(description = "ID único del envío", example = "1", accessMode = AccessMode.READ_ONLY)
    private Long shippingId;

    @Schema(description = "ID de la orden asociada", example = "1")
    private Long orderId;

    @Schema(description = "ID del usuario", example = "1")
    private Long userId;

    @Schema(description = "Dirección de envío", example = "Av. Siempre Viva 123")
    private String address;

    @Schema(description = "Estado del envío", example = "PENDING", allowableValues = {"PENDING", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELLED"})
    private EnviosStatus status;

    @Schema(description = "Número de seguimiento", example = "TRK39E1EDF7")
    private String trackingNumber;

    @Schema(description = "Fecha de creación", example = "2026-06-08T10:30:00", accessMode = AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de envío", example = "2026-06-09T10:30:00")
    private LocalDateTime shippedAt;

    @Schema(description = "Fecha estimada de entrega", example = "2026-06-14T10:30:00")
    private LocalDateTime estimatedDelivery;

    @Schema(description = "Fecha de entrega", example = "2026-06-12T10:30:00")
    private LocalDateTime deliveredAt;
}