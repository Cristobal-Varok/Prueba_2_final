package com.example.ms_envios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO para crear un nuevo envio")
public class CreateEnvioRequest {

    @NotNull(message = "El orderId es obligatorio")
    @Schema(description = "ID de la orden asociada" , example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotBlank(message = "La dirección de envío es obligatoria")
    @Schema(description = "Direccion de envio", example = "Av. Siempre Viva 123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
}