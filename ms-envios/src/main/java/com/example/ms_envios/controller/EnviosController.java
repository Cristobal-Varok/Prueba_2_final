package com.example.ms_envios.controller;

import com.example.ms_envios.dto.request.CreateEnvioRequest;
import com.example.ms_envios.dto.response.EnvioResponseDTO;
import com.example.ms_envios.service.EnviosService;
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
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
public class EnviosController {

    private final EnviosService enviosService;

    // Crear envío (solo para órdenes pagadas)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<EnvioResponseDTO> createShipping(Authentication authentication,
                                                           @Valid @RequestBody CreateEnvioRequest request) {
        String username = authentication.getName();
        log.info("Solicitud de creación de envío - Usuario: {}, Orden: {}", username, request.getOrderId());
        EnvioResponseDTO created = enviosService.createShipping(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Obtener envío por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<EnvioResponseDTO> getById(@PathVariable Long id) {
        log.info("Consultando envío con id: {}", id);
        return ResponseEntity.ok(enviosService.getShipping(id));
    }

    // Obtener envíos por orden
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<EnvioResponseDTO>> getByOrder(@PathVariable Long orderId) {
        log.info("Consultando envíos por orderId: {}", orderId);
        return ResponseEntity.ok(enviosService.getByOrder(orderId));
    }

    // Obtener mis envíos (por usuario autenticado)
    @GetMapping("/my-shippings")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<EnvioResponseDTO>> getMyShippings(Authentication authentication) {
        String username = authentication.getName();
        log.info("Consultando envíos del usuario: {}", username);
        // TODO: Obtener userId real desde ms_users
        Long userId = 1L;
        return ResponseEntity.ok(enviosService.getByUser(userId));
    }


}