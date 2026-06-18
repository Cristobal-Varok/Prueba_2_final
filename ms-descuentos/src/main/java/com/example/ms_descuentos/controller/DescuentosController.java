package com.example.ms_descuentos.controller;

import com.example.ms_descuentos.dto.response.DescuentosResult;
import com.example.ms_descuentos.service.DescuentosService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/descuentos")
@RequiredArgsConstructor
@Tag(name = "Descuentos", description = "Operaciones de uso y validación de cupones")
public class DescuentosController {

    private final DescuentosService descuentosService;

    @Operation(summary = "Usar cupón", description = "Aplica un cupón al total del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón aplicado correctamente"),
            @ApiResponse(responseCode = "400", description = "Cupón inválido o expirado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PostMapping("/use")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<DescuentosResult> useCoupon(@RequestParam String code,
                                                      @RequestParam Double cartTotal) {
        log.info("Usando cupón: {} con total carrito: ${}", code, cartTotal);
        DescuentosResult result = descuentosService.useCoupon(code, cartTotal);
        result.add(linkTo(methodOn(DescuentosController.class).useCoupon(code, cartTotal)).withSelfRel());
        result.add(linkTo(methodOn(DescuentosController.class).validateCoupon(code, cartTotal)).withRel("validar"));
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @Operation(summary = "Validar cupón", description = "Valida si un cupón es aplicable sin usarlo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validación realizada"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
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
        result.add(linkTo(methodOn(DescuentosController.class).validateCoupon(code, cartTotal)).withSelfRel());
        result.add(linkTo(methodOn(DescuentosController.class).useCoupon(code, cartTotal)).withRel("usar"));
        return ResponseEntity.ok(result);
    }
}