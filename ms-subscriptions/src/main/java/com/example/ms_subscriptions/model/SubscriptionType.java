package com.example.ms_subscriptions.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de suscripción disponibles")
public enum SubscriptionType {
    @Schema(description = "Suscripción básica")
    BASICA,
    @Schema(description = "Suscripción premium")
    PREMIUM,
    @Schema(description = "Suscripción VIP")
    VIP
}