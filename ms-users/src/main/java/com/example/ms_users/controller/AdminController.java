package com.example.ms_users.controller;

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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administración", description = "Endpoints para administradores del sistema")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ========== MÉTODO PRIVADO PARA HATEOAS ==========

    private UserResponseDTO addUserLinks(UserResponseDTO user) {
        // Self link
        user.add(linkTo(methodOn(AdminController.class).getUserByUsername(user.getUsername())).withSelfRel());

        // Link para actualizar usuario
        user.add(linkTo(methodOn(AdminController.class).updateUserByAdmin(user.getUsername(), null))
                .withRel("update"));

        // Link para cambiar rol
        user.add(linkTo(methodOn(AdminController.class).changeUserRole(user.getUsername(), null))
                .withRel("changeRole"));

        // Link para eliminar usuario
        user.add(linkTo(methodOn(AdminController.class).deleteUserByUsername(user.getUsername()))
                .withRel("delete"));

        // Link para buscar por email
        user.add(linkTo(methodOn(AdminController.class).searchUserByEmail(user.getMail()))
                .withRel("searchByEmail"));

        return user;
    }

    // ========== ENDPOINTS ==========

    @Operation(
            summary = "Obtener todos los usuarios",
            description = "Lista todos los usuarios registrados en el sistema. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es ADMIN)")
    })
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        log.info("ADMIN - Solicitud de listar todos los usuarios");
        List<UserResponseDTO> users = userService.getAllUsers();

        // Agregar enlaces HATEOAS a cada usuario
        List<UserResponseDTO> usersWithLinks = users.stream()
                .map(this::addUserLinks)
                .toList();

        // Enlaces de colección
        CollectionModel<UserResponseDTO> collection = CollectionModel.of(usersWithLinks);
        collection.add(linkTo(methodOn(AdminController.class).getAllUsers()).withSelfRel());
        collection.add(linkTo(methodOn(AdminController.class).getUsersByRole("ADMIN")).withRel("admins"));
        collection.add(linkTo(methodOn(AdminController.class).getUsersByRole("USER")).withRel("users"));

        log.debug("ADMIN - Total de usuarios encontrados: {}", users.size());
        return ResponseEntity.ok(Map.of(
                "users", collection,
                "total", users.size()
        ));
    }

    @Operation(
            summary = "Obtener usuario por username",
            description = "Obtiene la información de un usuario específico por su nombre de usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es ADMIN)")
    })
    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByUsername(
            @Parameter(description = "Nombre de usuario a buscar", required = true, example = "juan.perez")
            @PathVariable String username) {
        log.info("ADMIN - Solicitando información del usuario: {}", username);
        try {
            UserResponseDTO user = userService.getUserProfile(username);
            UserResponseDTO userWithLinks = addUserLinks(user);
            log.debug("ADMIN - Usuario encontrado: {}", username);
            return ResponseEntity.ok(Map.of("user", userWithLinks));
        } catch (RuntimeException e) {
            log.warn("ADMIN - Usuario no encontrado: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Obtener usuarios por rol",
            description = "Filtra los usuarios según su rol (ADMIN o USER)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios filtrados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Rol inválido"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es ADMIN)")
    })
    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(
            @Parameter(description = "Rol a filtrar", required = true, example = "ADMIN")
            @PathVariable String role) {
        log.info("ADMIN - Solicitando usuarios por rol: {}", role);
        try {
            List<UserResponseDTO> users = userService.getUsersByRole(role);

            List<UserResponseDTO> usersWithLinks = users.stream()
                    .map(this::addUserLinks)
                    .toList();

            CollectionModel<UserResponseDTO> collection = CollectionModel.of(usersWithLinks);
            collection.add(linkTo(methodOn(AdminController.class).getUsersByRole(role)).withSelfRel());
            collection.add(linkTo(methodOn(AdminController.class).getAllUsers()).withRel("allUsers"));

            log.debug("ADMIN - Usuarios con rol {} encontrados: {}", role, users.size());
            return ResponseEntity.ok(Map.of(
                    "role", role,
                    "users", collection,
                    "total", users.size()
            ));
        } catch (RuntimeException e) {
            log.warn("ADMIN - Rol inválido solicitado: {}", role);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Actualizar usuario por ADMIN",
            description = "Actualiza los datos de un usuario específico. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de email"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es ADMIN)")
    })
    @PutMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserByAdmin(
            @Parameter(description = "Nombre de usuario a actualizar", required = true, example = "juan.perez")
            @PathVariable String username,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("ADMIN - Actualizando usuario: {}, Datos: {}", username, updateDTO);
        try {
            UserResponseDTO updatedUser = userService.updateUser(username, updateDTO);
            UserResponseDTO userWithLinks = addUserLinks(updatedUser);
            log.info("ADMIN - Usuario actualizado exitosamente: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario actualizado correctamente por ADMIN",
                    "user", userWithLinks
            ));
        } catch (RuntimeException e) {
            log.error("ADMIN - Error actualizando usuario: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario del sistema. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es ADMIN)")
    })
    @DeleteMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByUsername(
            @Parameter(description = "Nombre de usuario a eliminar", required = true, example = "juan.perez")
            @PathVariable String username) {
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

    @Operation(
            summary = "Cambiar rol de usuario",
            description = "Cambia el rol de un usuario entre ADMIN y USER. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Rol inválido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es ADMIN)")
    })
    @PatchMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @Parameter(description = "Nombre de usuario", required = true, example = "juan.perez")
            @PathVariable String username,
            @Parameter(description = "Nuevo rol", required = true, example = "ADMIN")
            @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        log.info("ADMIN - Cambiando rol del usuario {} a: {}", username, newRole);
        try {
            UserResponseDTO updatedUser = userService.changeRole(username, newRole);
            UserResponseDTO userWithLinks = addUserLinks(updatedUser);
            log.info("ADMIN - Rol actualizado exitosamente para: {}", username);
            return ResponseEntity.ok(Map.of(
                    "message", "Rol actualizado correctamente",
                    "user", userWithLinks
            ));
        } catch (RuntimeException e) {
            log.error("ADMIN - Error cambiando rol para: {}", username, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Buscar usuario por email",
            description = "Busca un usuario por su correo electrónico. Solo accesible para ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (no es ADMIN)")
    })
    @GetMapping("/users/search/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUserByEmail(
            @Parameter(description = "Email a buscar", required = true, example = "juan.perez@email.com")
            @RequestParam String email) {
        log.info("ADMIN - Buscando usuario por email: {}", email);
        try {
            UserResponseDTO user = userService.getUserByEmail(email);
            UserResponseDTO userWithLinks = addUserLinks(user);
            log.debug("ADMIN - Usuario encontrado por email: {}", email);
            return ResponseEntity.ok(Map.of("user", userWithLinks));
        } catch (RuntimeException e) {
            log.warn("ADMIN - Usuario no encontrado por email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}