package com.example.ms_subscriptions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionDTO {

    @NotBlank(message = "El tipo de suscripción es requerido")
    private String type; // BASICA, PREMIUM, VIP

    @NotNull(message = "La duración en meses es requerida")
    private Integer durationMonths; // 1, 3, 6, 12
}