package com.example.ms_users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para registro de nuevos usuarios")
public class UserDTO {

    @Schema(description = "Identificador único del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre de usuario", example = "juan.perez", required = true)
    @NotBlank(message = "El username es requerido")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    @Schema(description = "Correo electrónico", example = "juan.perez@email.com", required = true)
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String mail;

    @Schema(description = "Contraseña", example = "MiPassword123", required = true)
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String password;

    @Schema(description = "Rol del usuario", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    @Schema(description = "Dirección del usuario", example = "Av. Principal 123, Santiago")
    private String address;
}