package com.example.ms_pagos.controller;

import com.example.ms_pagos.dto.response.PagosResponseDTO;
import com.example.ms_pagos.service.PagosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pagos/admin")
@RequiredArgsConstructor
public class PagosAdminController {

    private final PagosService pagosService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagosResponseDTO>> getUserPayments(@PathVariable Long userId) {
        log.info("ADMIN - Consultando pagos del usuario: {}", userId);
        List<PagosResponseDTO> payments = pagosService.getPaymentsByUser(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> paymentExistsByOrder(@PathVariable Long orderId) {
        log.debug("ADMIN - Verificando si existe pago para orden: {}", orderId);
        boolean exists = pagosService.paymentExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }
}