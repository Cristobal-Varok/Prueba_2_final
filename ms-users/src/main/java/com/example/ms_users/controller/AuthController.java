package com.example.ms_users.controller;

import com.example.ms_users.dto.UserResponseDTO;
import com.example.ms_users.model.User;
import com.example.ms_users.security.jwt.JwtService;
import com.example.ms_users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
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

    // ========== MÉTODO PRIVADO PARA HATEOAS ==========

    private UserResponseDTO addUserLinks(UserResponseDTO user) {
        user.add(linkTo(methodOn(UserController.class).getMyProfile(null)).withRel("profile"));
        user.add(linkTo(methodOn(UserController.class).updateMyProfile(null, null)).withRel("update"));
        user.add(linkTo(methodOn(UserController.class).updateAddress(null, null)).withRel("updateAddress"));
        user.add(linkTo(methodOn(UserController.class).deleteOwnAccount(null)).withRel("deleteAccount"));
        return user;
    }

    // ========== ENDPOINTS ==========

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Registra un nuevo usuario en el sistema. El rol por defecto es USER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de username/email")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Parameter(description = "Datos de registro del usuario")
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String mail = body.get("mail");
        String role = body.getOrDefault("role", "USER");
        String address = body.get("address");

        log.info("Intento de registro - Usuario: {}, Email: {}, Rol: {}", username, mail, role);

        User user = userService.register(username, password, role, mail, address);

        log.info("Registro exitoso - Usuario creado: {} con rol: {}", username, user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Usuario registrado correctamente",
                "role", user.getRole(),
                "username", user.getUsername(),
                "mail", user.getMail(),
                "address", user.getAddress() != null ? user.getAddress() : "No especificada"
        ));
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario y retorna un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso - Retorna token JWT"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "Credenciales de login (username, password)")
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        log.info("Intento de login - Usuario: {}", username);

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            if (auth.isAuthenticated()) {
                UserResponseDTO user = userService.getUserProfile(username);
                String token = jwtService.generateToken(username, user.getRole());

                // Agregar enlaces HATEOAS
                UserResponseDTO userWithLinks = addUserLinks(user);

                log.info("Login exitoso - Usuario: {}, Rol: {}", username, user.getRole());

                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "username", username,
                        "role", user.getRole(),
                        "mail", user.getMail(),
                        "address", user.getAddress() != null ? user.getAddress() : "",
                        "links", userWithLinks.getLinks()
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

    @Operation(
            summary = "Validar token JWT",
            description = "Verifica si el token JWT enviado en la petición es válido"
    )
    @ApiResponse(responseCode = "200", description = "Token válido")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        log.debug("Validación de token solicitada");
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @Operation(
            summary = "Verificar existencia de usuario",
            description = "Verifica si un nombre de usuario ya está registrado en el sistema. Endpoint público."
    )
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    @GetMapping("/user-exists/{username}")
    public ResponseEntity<?> userExists(
            @Parameter(description = "Nombre de usuario a verificar", required = true, example = "juan.perez")
            @PathVariable String username) {
        boolean exists = userService.findByUsername(username).isPresent();
        log.debug("Verificación de existencia - Usuario: {}, Existe: {}", username, exists);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(
            summary = "Obtener ID de usuario por username",
            description = "Devuelve el ID interno de un usuario a partir de su username. Endpoint público, usado por otros microservicios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ID del usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/user-id/{username}")
    public ResponseEntity<?> getUserId(
            @Parameter(description = "Nombre de usuario", required = true, example = "juan.perez")
            @PathVariable String username) {
        return userService.findByUsername(username)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(Map.of("id", user.getId())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado: " + username)));
    }

}