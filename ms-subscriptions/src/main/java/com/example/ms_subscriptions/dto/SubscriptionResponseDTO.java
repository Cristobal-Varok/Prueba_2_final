package com.example.ms_subscriptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de respuesta con datos de la suscripción")
public class SubscriptionResponseDTO extends RepresentationModel<SubscriptionResponseDTO> {

    @Schema(description = "ID único de la suscripción", example = "1", accessMode = READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del usuario", example = "javier")
    private String username;

    @Schema(description = "Tipo de suscripción", example = "PREMIUM")
    private String type;

    @Schema(description = "Fecha de inicio", example = "2026-06-08T10:30:00", accessMode = READ_ONLY)
    private LocalDateTime startDate;

    @Schema(description = "Fecha de fin", example = "2026-07-08T10:30:00")
    private LocalDateTime endDate;

    @Schema(description = "Estado de la suscripción", example = "true")
    private Boolean active;
}