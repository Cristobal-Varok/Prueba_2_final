package com.example.ms_envios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEnvioRequest {

    @NotNull(message = "El orderId es obligatorio")
    private Long orderId;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String address;
}