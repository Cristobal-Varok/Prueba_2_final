package com.example.ms_users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta con la información del usuario")
public class UserResponseDTO extends RepresentationModel<UserResponseDTO> {

    @Schema(description = "Identificador único del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre de usuario", example = "juan.perez")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@email.com")
    private String mail;

    @Schema(description = "Rol del usuario en el sistema", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    @Schema(description = "Dirección del usuario", example = "Av. Principal 123, Santiago")
    private String address;
}