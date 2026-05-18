package com.example.ms_envios.controller;

import com.example.ms_envios.dto.response.EnvioResponseDTO;
import com.example.ms_envios.model.EnviosStatus;
import com.example.ms_envios.service.EnviosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/envios/admin")
@RequiredArgsConstructor
public class EnviosAdminController {

    private final EnviosService enviosService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnvioResponseDTO>> getUserShippings(@PathVariable Long userId) {
        log.info("ADMIN - Consultando envíos del usuario: {}", userId);
        return ResponseEntity.ok(enviosService.getByUser(userId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnvioResponseDTO>> getByStatus(@PathVariable EnviosStatus status) {
        log.info("ADMIN - Consultando envíos por estado: {}", status);
        return ResponseEntity.ok(enviosService.getByStatus(status));
    }

    @GetMapping("/exists/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> shippingExistsByOrder(@PathVariable Long orderId) {
        log.debug("ADMIN - Verificando si existe envío para orden: {}", orderId);
        boolean exists = enviosService.shippingExistsByOrder(orderId);
        return ResponseEntity.ok(exists);
    }

    // Actualizar estado del envío (solo ADMIN)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<EnvioResponseDTO> updateStatus(@PathVariable Long id,
                                                         @RequestParam EnviosStatus status) {
        log.info("Actualizando estado del envío {} a {}", id, status);
        EnvioResponseDTO updated = enviosService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}