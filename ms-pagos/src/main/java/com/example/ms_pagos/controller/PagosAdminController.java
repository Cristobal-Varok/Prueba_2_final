package com.example.ms_pagos.controller;

import com.example.ms_pagos.dto.response.PagosResponseDTO;
import com.example.ms_pagos.service.PagosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pagos/admin")
@RequiredArgsConstructor
@Tag(name = "Pagos Admin", description = "Operaciones administrativas de pagos")
public class PagosAdminController {

    private final PagosService pagosService;

    @Operation(summary = "Obtener pagos por usuario", description = "Retorna todos los pagos de un usuario (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagosResponseDTO>> getUserPayments(@PathVariable Long userId) {
        log.info("ADMIN - Consultando pagos del usuario: {}", userId);
        List<PagosResponseDTO> payments = pagosService.getPaymentsByUser(userId);
        payments.forEach(p ->
                p.add(linkTo(methodOn(PagosController.class).getPaymentById(p.getPaymentId())).withSelfRel())
        );
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Verificar pago por orden", description = "Verifica si existe un pago para una orden (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> paymentExistsByOrder(@PathVariable Long orderId) {
        log.debug("ADMIN - Verificando si existe pago para orden: {}", orderId);
        boolean exists = pagosService.paymentExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }
}