package com.example.ms_users.controller;

import com.example.ms_users.dto.UserAddressDTO;
import com.example.ms_users.dto.UserResponseDTO;
import com.example.ms_users.dto.UserUpdateDTO;
import com.example.ms_users.exception.custom.EmailAlreadyExistsException;
import com.example.ms_users.exception.custom.InvalidRoleException;
import com.example.ms_users.exception.custom.UsernameAlreadyExistsException;
import com.example.ms_users.model.User;
import com.example.ms_users.security.jwt.JwtService;
import com.example.ms_users.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UserService userService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    // ========== ENDPOINTS PÚBLICOS ==========

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String mail = body.get("mail");
        String role = body.getOrDefault("role", "USER");
        String address = body.get("address");

        log.info("Intento de registro - Usuario: {}, Email: {}, Rol: {}", username, mail, role);

        if (username == null || password == null || mail == null ||
                username.isBlank() || password.isBlank() || mail.isBlank()) {
            log.warn("Registro fallido - Campos requeridos faltantes para usuario: {}", username);
            throw new IllegalArgumentException("Username, mail y password son requeridos");
        }

        if (!role.equals("USER") && !role.equals("ADMIN")) {
            log.warn("Registro fallido - Rol inválido: {} para usuario: {}", role, username);
            throw new InvalidRoleException("Rol inválido. Use: USER o ADMIN");
        }

        // Verificar si el username ya existe
        if (userService.findByUsername(username).isPresent()) {
            log.warn("Registro fallido - Username ya existe: {}", username);
            throw new UsernameAlreadyExistsException("El username '" + username + "' ya está en uso");
        }

        // Verificar si el email ya existe
        if (userService.findByMail(mail).isPresent()) {
            log.warn("Registro fallido - Email ya existe: {}", mail);
            throw new EmailAlreadyExistsException("El email '" + mail + "' ya está registrado");
        }

        User user = userService.register(username, password, role, mail, address);
        log.info("Registro exitoso - Usuario creado: {} con rol: {}", username, role);

        return ResponseEntity.ok(Map.of(
                "message", "Usuario registrado correctamente",
                "role", role,
                "username", user.getUsername(),
                "mail", user.getMail(),
                "address", user.getAddress() != null ? user.getAddress() : "No especificada"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        log.info("Intento de login - Usuario: {}", username);

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            if (auth.isAuthenticated()) {
                UserResponseDTO user = userService.getUserProfile(username);
                String token = jwtService.generateToken(username, user.getRole());

                log.info("Login exitoso - Usuario: {}, Rol: {}", username, user.getRole());

                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "username", username,
                        "role", user.getRole(),
                        "mail", user.getMail(),
                        "address", user.getAddress() != null ? user.getAddress() : ""
                ));
            }

            log.warn("Login fallido - Usuario no autenticado: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));

        } catch (BadCredentialsException e) {
            log.warn("Login fallido - Credenciales incorrectas para usuario: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
        } catch (Exception e) {
            log.error("Login fallido - Error interno para usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar la solicitud"));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        log.debug("Validación de token solicitada");
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @GetMapping("/user-exists/{username}")
    public ResponseEntity<?> userExists(@PathVariable String username) {
        boolean exists = userService.findByUsername(username).isPresent();
        log.debug("Verificación de existencia - Usuario: {}, Existe: {}", username, exists);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}