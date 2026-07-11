package com.example.ms_users.controller;

import com.example.ms_users.dto.UserAddressDTO;
import com.example.ms_users.dto.UserResponseDTO;
import com.example.ms_users.dto.UserUpdateDTO;
import com.example.ms_users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "Usuarios", description = "Endpoints para gestión de perfiles de usuario")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ========== MÉTODO PRIVADO PARA HATEOAS ==========

    private UserResponseDTO addUserLinks(UserResponseDTO user) {
        // Self link
        user.add(linkTo(methodOn(UserController.class).getMyProfile(null)).withSelfRel());

        // Link para actualizar perfil
        user.add(linkTo(methodOn(UserController.class).updateMyProfile(null, null))
                .withRel("update"));

        // Link para actualizar dirección
        user.add(linkTo(methodOn(UserController.class).updateAddress(null, null))
                .withRel("updateAddress"));

        // Link para eliminar cuenta
        user.add(linkTo(methodOn(UserController.class).deleteOwnAccount(null))
                .withRel("deleteAccount"));

        // Link para volver al perfil
        user.add(linkTo(methodOn(UserController.class).getMyProfile(null))
                .withRel("profile"));

        return user;
    }

    // ========== ENDPOINTS ==========

    @Operation(
            summary = "Obtener mi perfil",
            description = "Obtiene los datos del usuario autenticado actualmente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener perfil - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.info("Obteniendo perfil - Usuario: {}", username);

        try {
            UserResponseDTO profile = userService.getUserProfile(username);
            UserResponseDTO profileWithLinks = addUserLinks(profile);
            log.debug("Perfil obtenido exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of("user", profileWithLinks));
        } catch (RuntimeException e) {
            log.error("Error obteniendo perfil - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Actualizar mi perfil",
            description = "Actualiza los datos del usuario autenticado (email y/o dirección)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de email"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de actualizar perfil - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.info("Actualizando perfil - Usuario: {}, Nuevo email: {}, Nueva dirección: {}",
                username, updateDTO.getMail(), updateDTO.getAddress());

        try {
            UserResponseDTO updatedUser = userService.updateUser(username, updateDTO);
            UserResponseDTO userWithLinks = addUserLinks(updatedUser);
            log.info("Perfil actualizado exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Perfil actualizado correctamente",
                    "user", userWithLinks
            ));
        } catch (RuntimeException e) {
            log.error("Error actualizando perfil - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Actualizar mi dirección",
            description = "Actualiza solo la dirección del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección actualizada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/address")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateAddress(
            Authentication authentication,
            @Valid @RequestBody UserAddressDTO addressDTO) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de actualizar dirección - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.info("Actualizando dirección - Usuario: {}, Nueva dirección: {}", username, addressDTO.getAddress());

        try {
            UserResponseDTO updatedUser = userService.updateAddress(username, addressDTO.getAddress());
            UserResponseDTO userWithLinks = addUserLinks(updatedUser);
            log.info("Dirección actualizada exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Dirección actualizada correctamente",
                    "user", userWithLinks
            ));
        } catch (RuntimeException e) {
            log.error("Error actualizando dirección - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Eliminar mi cuenta",
            description = "Elimina la cuenta del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteOwnAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de eliminar cuenta - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.warn("Solicitud de eliminación de cuenta - Usuario: {}", username);

        try {
            userService.deleteOwnAccount(username);
            log.info("Cuenta eliminada exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Tu cuenta ha sido eliminada correctamente",
                    "username", username
            ));
        } catch (Exception e) {
            log.error("Error eliminando cuenta - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}