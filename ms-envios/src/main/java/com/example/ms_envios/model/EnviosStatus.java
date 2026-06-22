package com.example.ms_envios.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de un envío")
public enum EnviosStatus {
    @Schema(description = "Envío pendiente de procesamiento")
    PENDING,
    @Schema(description = "Envío en preparación")
    PREPARING,
    @Schema(description = "Envío despachado")
    SHIPPED,
    @Schema(description = "Envío en tránsito")
    IN_TRANSIT,
    @Schema(description = "Envío entregado")
    DELIVERED,
    @Schema(description = "Envío cancelado")
    CANCELLED
}