package com.example.ms_descuentos.controller;

import com.example.ms_descuentos.dto.response.DescuentosResult;
import com.example.ms_descuentos.service.DescuentosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/descuentos")
@RequiredArgsConstructor
public class DescuentosController {

    private final DescuentosService descuentosService;

    @PostMapping("/use")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<DescuentosResult> useCoupon(@RequestParam String code,
                                                      @RequestParam Double cartTotal) {
        log.info("Usando cupón: {} con total carrito: ${}", code, cartTotal);
        DescuentosResult result = descuentosService.useCoupon(code, cartTotal);
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/validate/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<DescuentosResult> validateCoupon(@PathVariable String code,
                                                           @RequestParam(required = false) Double cartTotal) {
        log.info("Validando cupón: {} con total carrito: ${}", code, cartTotal != null ? cartTotal : 0);

        com.example.ms_descuentos.dto.request.ValidateCouponRequest request =
                new com.example.ms_descuentos.dto.request.ValidateCouponRequest();
        request.setCode(code);
        request.setCartTotal(cartTotal != null ? cartTotal : 0.0);

        DescuentosResult result = descuentosService.validateCoupon(request);
        return ResponseEntity.ok(result);
    }
}