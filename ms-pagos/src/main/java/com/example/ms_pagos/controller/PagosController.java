package com.example.ms_pagos.controller;

import com.example.ms_pagos.dto.request.PagosRequestDTO;
import com.example.ms_pagos.dto.response.PagosResponseDTO;
import com.example.ms_pagos.service.PagosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagosController {

    private final PagosService pagosService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<PagosResponseDTO> processPayment(Authentication authentication,
                                                           @Valid @RequestBody PagosRequestDTO request) {
        String username = authentication.getName();
        log.info("Solicitud de pago - Usuario: {}, OrderId: {}", username, request.getOrderId());
        PagosResponseDTO response = pagosService.processPayment(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<PagosResponseDTO> getPaymentById(@PathVariable Long paymentId) {
        log.info("Consultando pago con id: {}", paymentId);
        PagosResponseDTO response = pagosService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<PagosResponseDTO>> getPaymentsByOrder(@PathVariable Long orderId) {
        log.info("Consultando pagos para orden: {}", orderId);
        List<PagosResponseDTO> response = pagosService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(response);
    }
}