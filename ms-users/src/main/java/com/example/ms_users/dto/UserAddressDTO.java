package com.example.ms_users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO para actualizar solo la dirección del usuario")
public class UserAddressDTO {

    @Schema(description = "Nueva dirección del usuario", example = "Av. Siempre Viva 742, Springfield", required = true)
    @NotBlank(message = "La dirección es requerida")
    private String address;
}