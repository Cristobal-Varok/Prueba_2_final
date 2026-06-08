package com.example.ms_subscriptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO para crear o renovar una suscripción")
public class SubscriptionDTO {

    @NotBlank(message = "El tipo de suscripción es requerido")
    @Schema(description = "Tipo de suscripción", example = "PREMIUM", allowableValues = {"BASICA", "PREMIUM", "VIP"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotNull(message = "La duración en meses es requerida")
    @Schema(description = "Duración en meses", example = "3", minimum = "1", maximum = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMonths;
}