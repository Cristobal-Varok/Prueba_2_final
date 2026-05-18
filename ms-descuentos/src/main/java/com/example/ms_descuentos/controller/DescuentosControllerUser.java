package com.example.ms_descuentos.controller;

import com.example.ms_descuentos.dto.response.DescuentosResponseDTO;
import com.example.ms_descuentos.service.DescuentosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts/user")
@RequiredArgsConstructor
public class DescuentosControllerUser {

    private final DescuentosService descuentosService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<DescuentosResponseDTO>> listAll() {
        log.info("Listando todos los cupones");
        return ResponseEntity.ok(descuentosService.listAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<DescuentosResponseDTO>> listActive() {
        log.info("Listando cupones activos");
        return ResponseEntity.ok(descuentosService.listActiveCoupons());
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<DescuentosResponseDTO> getByCode(@PathVariable String code) {
        log.info("Obteniendo cupón por código: {}", code);
        return ResponseEntity.ok(descuentosService.getCouponByCode(code));
    }

    @GetMapping("/exists/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable String code) {
        log.debug("Verificando existencia de cupón por código: {}", code);
        boolean exists = descuentosService.couponExists(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}