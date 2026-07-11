package com.example.ms_users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(description = "DTO para actualizar datos del usuario")
public class UserUpdateDTO {

    @Schema(description = "Nuevo correo electrónico", example = "nuevo.email@email.com")
    @Email(message = "El email debe ser válido")
    private String mail;

    @Schema(description = "Nueva dirección del usuario", example = "Nueva Dirección 456, Santiago")
    private String address;
}