package com.example.ms_envios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_shippings")
@Schema(description = "Entidad que representa un envío")
public class Envios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del envío", example = "1", accessMode = AccessMode.READ_ONLY)
    private Long shippingId;

    @Column(nullable = false)
    @Schema(description = "ID de la orden asociada", example = "1")
    private Long orderId;

    @Schema(description = "ID del usuario", example = "101")
    private Long userId;

    @Column(nullable = false)
    @Schema(description = "Dirección de envío", example = "Av. Siempre Viva 123")
    private String address;

    @Enumerated(EnumType.STRING)
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = EnviosStatus.PENDING;
        if (this.trackingNumber == null) {
            this.trackingNumber = "TRK" + System.currentTimeMillis();
        }
    }
}