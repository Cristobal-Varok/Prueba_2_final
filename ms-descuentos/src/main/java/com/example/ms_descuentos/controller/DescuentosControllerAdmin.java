package com.example.ms_descuentos.controller;

import com.example.ms_descuentos.dto.request.CreateCouponRequest;
import com.example.ms_descuentos.dto.response.DescuentosResponseDTO;
import com.example.ms_descuentos.service.DescuentosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/descuentos/admin")
@RequiredArgsConstructor
public class DescuentosControllerAdmin {

    private final DescuentosService descuentosService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentosResponseDTO> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón: {}", request.getCode());
        DescuentosResponseDTO created = descuentosService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentosResponseDTO> createCouponForUser(@PathVariable String username,
                                                                     @Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón para usuario: {} con código: {}", username, request.getCode());
        DescuentosResponseDTO created = descuentosService.createCouponForUser(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        log.info("ADMIN - Desactivando cupón con id: {}", id);
        descuentosService.deactivateCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentosResponseDTO> activate(@PathVariable Long id) {
        log.info("ADMIN - Activando cupón con id: {}", id);
        descuentosService.activateCoupon(id);
        DescuentosResponseDTO coupon = descuentosService.getCouponByCode(
                descuentosService.findByIdOrThrow(id).getCode());
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable Long id) {
        log.debug("Verificando existencia de cupón por ID: {}", id);
        boolean exists = descuentosService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}