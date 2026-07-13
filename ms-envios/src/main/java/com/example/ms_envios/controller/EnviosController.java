package com.example.ms_envios.controller;

import com.example.ms_envios.dto.CreateEnvioRequest;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
@Tag(name = "Envíos (Usuario)", description = "Endpoints para que los usuarios gestionen sus envíos")
@SecurityRequirement(name = "bearerAuth")
public class EnviosController {

    private final EnviosService enviosService;

    // ========== MÉTODOS PRIVADOS PARA HATEOAS ==========

    private EnvioResponseDTO addLinks(EnvioResponseDTO dto) {
        // Self link
        dto.add(linkTo(methodOn(EnviosController.class).getById(dto.getShippingId())).withSelfRel());

        // Link a la orden asociada (si se conoce el orderId)
        dto.add(linkTo(methodOn(EnviosController.class).getByOrder(dto.getOrderId())).withRel("order"));

        // Link a mis envíos
        dto.add(linkTo(methodOn(EnviosController.class).getMyShippings(null)).withRel("myShippings"));

        // Link para actualizar estado (solo ADMIN, pero se muestra como posible acción)
        dto.add(linkTo(methodOn(EnviosAdminController.class).updateStatus(dto.getShippingId(), dto.getStatus()))
                .withRel("updateStatus"));

        return dto;
    }

    private CollectionModel<EnvioResponseDTO> addCollectionLinks(List<EnvioResponseDTO> list) {
        List<EnvioResponseDTO> dtos = list.stream()
                .map(this::addLinks)
                .toList();

        CollectionModel<EnvioResponseDTO> collection = CollectionModel.of(dtos);
        collection.add(linkTo(methodOn(EnviosController.class).getMyShippings(null)).withSelfRel());
        return collection;
    }

    // ========== ENDPOINTS ==========

    @Operation(
            summary = "Crear un nuevo envío",
            description = "Crea un envío para una orden pagada. La orden debe estar en estado PAID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envío creado exitosamente",
                    content = @Content(schema = @Schema(implementation = EnvioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o orden no pagada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Orden o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un envío para esta orden")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<EnvioResponseDTO> createShipping(
            Authentication authentication,
            @Valid @RequestBody CreateEnvioRequest request) {
        String username = authentication.getName();
        log.info("Solicitud de creación de envío - Usuario: {}, Orden: {}", username, request.getOrderId());
        EnvioResponseDTO created = enviosService.createShipping(username, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addLinks(created));
    }

    @Operation(
            summary = "Obtener un envío por ID",
            description = "Retorna los detalles de un envío específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envío encontrado",
                    content = @Content(schema = @Schema(implementation = EnvioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<EnvioResponseDTO> getById(
            @Parameter(description = "ID del envío", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Consultando envío con id: {}", id);
        EnvioResponseDTO dto = enviosService.getShipping(id);
        return ResponseEntity.ok(addLinks(dto));
    }

    @Operation(
            summary = "Obtener envíos por orden",
            description = "Retorna todos los envíos asociados a una orden específica."
    )
    @ApiResponse(responseCode = "200", description = "Lista de envíos")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CollectionModel<EnvioResponseDTO>> getByOrder(
            @Parameter(description = "ID de la orden", required = true, example = "1")
            @PathVariable Long orderId) {
        log.info("Consultando envíos por orderId: {}", orderId);
        List<EnvioResponseDTO> list = enviosService.getByOrder(orderId);
        return ResponseEntity.ok(addCollectionLinks(list));
    }

    @Operation(
            summary = "Obtener mis envíos",
            description = "Retorna todos los envíos del usuario autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Lista de envíos del usuario")
    @GetMapping("/my-shippings")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CollectionModel<EnvioResponseDTO>> getMyShippings(
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Consultando envíos del usuario: {}", username);
        // TODO: Obtener userId real desde ms_users
        Long userId = 1L;
        List<EnvioResponseDTO> list = enviosService.getByUser(userId);
        return ResponseEntity.ok(addCollectionLinks(list));
    }
}