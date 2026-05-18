package com.example.ms_users.controller;

import com.example.ms_users.dto.UserAddressDTO;
import com.example.ms_users.dto.UserResponseDTO;
import com.example.ms_users.dto.UserUpdateDTO;
import com.example.ms_users.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ========== ENDPOINTS PARA USUARIOS AUTENTICADOS ==========

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
            log.debug("Perfil obtenido exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of("user", profile));
        } catch (RuntimeException e) {
            log.error("Error obteniendo perfil - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateMyProfile(Authentication authentication, @Valid @RequestBody UserUpdateDTO updateDTO) {
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
            log.info("Perfil actualizado exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Perfil actualizado correctamente",
                    "user", updatedUser
            ));
        } catch (RuntimeException e) {
            log.error("Error actualizando perfil - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/address")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateAddress(Authentication authentication, @Valid @RequestBody UserAddressDTO addressDTO) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de actualizar dirección - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.info("Actualizando dirección - Usuario: {}, Nueva dirección: {}", username, addressDTO.getAddress());

        try {
            UserResponseDTO updatedUser = userService.updateAddress(username, addressDTO.getAddress());
            log.info("Dirección actualizada exitosamente - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Dirección actualizada correctamente",
                    "user", updatedUser
            ));
        } catch (RuntimeException e) {
            log.error("Error actualizando dirección - Usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

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