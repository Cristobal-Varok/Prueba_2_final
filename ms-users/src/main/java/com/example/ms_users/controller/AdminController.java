package com.example.ms_users.controller;

import com.example.ms_users.dto.UserResponseDTO;
import com.example.ms_users.dto.UserUpdateDTO;
import com.example.ms_users.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ========== ENDPOINTS PARA ADMIN ==========

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        log.info("ADMIN - Solicitud de listar todos los usuarios");
        List<UserResponseDTO> users = userService.getAllUsers();
        log.debug("ADMIN - Total de usuarios encontrados: {}", users.size());
        return ResponseEntity.ok(Map.of(
                "users", users,
                "total", users.size()
        ));
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        log.info("ADMIN - Solicitando información del usuario: {}", username);
        try {
            UserResponseDTO user = userService.getUserProfile(username);
            log.debug("ADMIN - Usuario encontrado: {}", username);
            return ResponseEntity.ok(Map.of("user", user));
        } catch (RuntimeException e) {
            log.warn("ADMIN - Usuario no encontrado: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        log.info("ADMIN - Solicitando usuarios por rol: {}", role);
        try {
            List<UserResponseDTO> users = userService.getUsersByRole(role);
            log.debug("ADMIN - Usuarios con rol {} encontrados: {}", role, users.size());
            return ResponseEntity.ok(Map.of(
                    "role", role,
                    "users", users,
                    "total", users.size()
            ));
        } catch (RuntimeException e) {
            log.warn("ADMIN - Rol inválido solicitado: {}", role);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserByAdmin(@PathVariable String username,
                                               @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("ADMIN - Actualizando usuario: {}, Datos: {}", username, updateDTO);
        try {
            UserResponseDTO updatedUser = userService.updateUser(username, updateDTO);
            log.info("ADMIN - Usuario actualizado exitosamente: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario actualizado correctamente por ADMIN",
                    "user", updatedUser
            ));
        } catch (RuntimeException e) {
            log.error("ADMIN - Error actualizando usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username) {
        log.warn("ADMIN - Eliminando usuario: {}", username);
        try {
            userService.deleteByUsername(username);
            log.info("ADMIN - Usuario eliminado exitosamente: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario eliminado correctamente",
                    "username", username
            ));
        } catch (Exception e) {
            log.error("ADMIN - Error eliminando usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(@PathVariable String username,
                                            @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        log.info("ADMIN - Cambiando rol del usuario {} a: {}", username, newRole);
        try {
            UserResponseDTO updatedUser = userService.changeRole(username, newRole);
            log.info("ADMIN - Rol actualizado exitosamente para: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Rol actualizado correctamente",
                    "user", updatedUser
            ));
        } catch (RuntimeException e) {
            log.error("ADMIN - Error cambiando rol para: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/search/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUserByEmail(@RequestParam String email) {
        log.info("ADMIN - Buscando usuario por email: {}", email);
        try {
            UserResponseDTO user = userService.getUserByEmail(email);
            log.debug("ADMIN - Usuario encontrado por email: {}", email);
            return ResponseEntity.ok(Map.of("user", user));
        } catch (RuntimeException e) {
            log.warn("ADMIN - Usuario no encontrado por email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}