package com.example.ms_pagos.controller;

import com.example.ms_pagos.dto.request.PagosRequestDTO;
import com.example.ms_pagos.dto.response.PagosResponseDTO;
import com.example.ms_pagos.service.PagosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Operaciones de procesamiento de pagos")
public class PagosController {

    private final PagosService pagosService;

    @Operation(summary = "Procesar pago", description = "Procesa un pago para una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago procesado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<PagosResponseDTO> processPayment(Authentication authentication,
                                                           @Valid @RequestBody PagosRequestDTO request) {
        String username = authentication.getName();
        log.info("Solicitud de pago - Usuario: {}, OrderId: {}", username, request.getOrderId());
        PagosResponseDTO response = pagosService.processPayment(username, request);
        response.add(linkTo(methodOn(PagosController.class).processPayment(authentication, null)).withSelfRel());
        response.add(linkTo(methodOn(PagosController.class).getPaymentById(response.getPaymentId())).withRel("detalle"));
        response.add(linkTo(methodOn(PagosController.class).getPaymentsByOrder(request.getOrderId())).withRel("pagos-orden"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener pago por ID", description = "Retorna los datos de un pago por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<PagosResponseDTO> getPaymentById(@PathVariable Long paymentId) {
        log.info("Consultando pago con id: {}", paymentId);
        PagosResponseDTO response = pagosService.getPaymentById(paymentId);
        response.add(linkTo(methodOn(PagosController.class).getPaymentById(paymentId)).withSelfRel());
        response.add(linkTo(methodOn(PagosController.class).getPaymentsByOrder(response.getOrderId())).withRel("pagos-orden"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener pagos por orden", description = "Retorna todos los pagos asociados a una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<PagosResponseDTO>> getPaymentsByOrder(@PathVariable Long orderId) {
        log.info("Consultando pagos para orden: {}", orderId);
        List<PagosResponseDTO> response = pagosService.getPaymentsByOrder(orderId);
        response.forEach(p ->
                p.add(linkTo(methodOn(PagosController.class).getPaymentById(p.getPaymentId())).withSelfRel())
        );
        return ResponseEntity.ok(response);
    }
}