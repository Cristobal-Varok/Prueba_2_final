package com.example.ms_pagos.dto.request;

import com.example.ms_pagos.model.PagosMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Datos para procesar un pago")
public class PagosRequestDTO {

    @NotNull(message = "El orderId es obligatorio")
    @Schema(description = "ID de la orden a pagar", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotNull(message = "El amount es obligatorio")
    @Positive(message = "El amount debe ser mayor a cero")
    @Schema(description = "Monto a pagar", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double amount;

    @NotNull(message = "El método de pago es obligatorio")
    @Schema(description = "Método de pago", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    private PagosMethod method;
}