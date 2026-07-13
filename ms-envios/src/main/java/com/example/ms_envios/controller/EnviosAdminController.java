package com.example.ms_envios.controller;

import com.example.ms_envios.dto.response.EnvioResponseDTO;
import com.example.ms_envios.model.EnviosStatus;
import com.example.ms_envios.service.EnviosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/envios/admin")
@RequiredArgsConstructor
@Tag(name = "Envíos (Administración)", description = "Endpoints para administración de envíos (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class EnviosAdminController {

    private final EnviosService enviosService;

    // ========== MÉTODOS PRIVADOS PARA HATEOAS ==========

    /**
     * Agrega enlaces HATEOAS a un DTO individual
     */
    private EnvioResponseDTO addLinks(EnvioResponseDTO dto) {
        // Self link
        dto.add(linkTo(methodOn(EnviosAdminController.class).getUserShippings(dto.getUserId())).withRel("userShippings"));

        // Link para filtrar por el mismo estado
        dto.add(linkTo(methodOn(EnviosAdminController.class).getByStatus(dto.getStatus())).withRel("sameStatus"));

        // Link para actualizar estado
        dto.add(linkTo(methodOn(EnviosAdminController.class).updateStatus(dto.getShippingId(), dto.getStatus()))
                .withRel("updateStatus"));

        // Link a la orden asociada
        dto.add(linkTo(methodOn(EnviosController.class).getByOrder(dto.getOrderId())).withRel("order"));

        return dto;
    }

    /**
     * Agrega enlaces a una colección de DTOs
     */
    private CollectionModel<EnvioResponseDTO> addCollectionLinks(List<EnvioResponseDTO> list, Link selfLink) {
        List<EnvioResponseDTO> dtos = list.stream()
                .map(this::addLinks)
                .toList();
        CollectionModel<EnvioResponseDTO> collection = CollectionModel.of(dtos);
        collection.add(selfLink);
        return collection;
    }

    // ========== ENDPOINTS ==========

    @Operation(
            summary = "Obtener envíos por usuario (ADMIN)",
            description = "Retorna todos los envíos de un usuario específico."
    )
    @ApiResponse(responseCode = "200", description = "Lista de envíos del usuario")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EnvioResponseDTO>> getUserShippings(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long userId) {
        log.info("ADMIN - Consultando envíos del usuario: {}", userId);
        List<EnvioResponseDTO> list = enviosService.getByUser(userId);
        Link selfLink = linkTo(methodOn(EnviosAdminController.class).getUserShippings(userId)).withSelfRel();
        return ResponseEntity.ok(addCollectionLinks(list, selfLink));
    }

    @Operation(
            summary = "Obtener envíos por estado (ADMIN)",
            description = "Filtra los envíos según su estado actual."
    )
    @ApiResponse(responseCode = "200", description = "Lista de envíos filtrados")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EnvioResponseDTO>> getByStatus(
            @Parameter(description = "Estado del envío", required = true, example = "PENDING")
            @PathVariable EnviosStatus status) {
        log.info("ADMIN - Consultando envíos por estado: {}", status);
        List<EnvioResponseDTO> list = enviosService.getByStatus(status);
        Link selfLink = linkTo(methodOn(EnviosAdminController.class).getByStatus(status)).withSelfRel();
        return ResponseEntity.ok(addCollectionLinks(list, selfLink));
    }

    @Operation(
            summary = "Verificar si existe envío para una orden (ADMIN)",
            description = "Retorna true si ya existe un envío para la orden especificada."
    )
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> shippingExistsByOrder(
            @Parameter(description = "ID de la orden", required = true, example = "1")
            @PathVariable Long orderId) {
        log.debug("ADMIN - Verificando si existe envío para orden: {}", orderId);
        boolean exists = enviosService.shippingExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }

    @Operation(
            summary = "Actualizar estado de un envío (ADMIN)",
            description = "Cambia el estado de un envío."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = EnvioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido para la transición")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<EnvioResponseDTO> updateStatus(
            @Parameter(description = "ID del envío", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado", required = true, example = "SHIPPED")
            @RequestParam EnviosStatus status) {
        log.info("Actualizando estado del envío {} a {}", id, status);
        EnvioResponseDTO updated = enviosService.updateStatus(id, status);
        return ResponseEntity.ok(addLinks(updated));
    }
}