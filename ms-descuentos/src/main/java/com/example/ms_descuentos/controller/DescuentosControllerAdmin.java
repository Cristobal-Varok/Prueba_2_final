package com.example.ms_descuentos.controller;

import com.example.ms_descuentos.dto.request.CreateCouponRequest;
import com.example.ms_descuentos.dto.response.DescuentosResponseDTO;
import com.example.ms_descuentos.service.DescuentosService;
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
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/descuentos/admin")
@RequiredArgsConstructor
@Tag(name = "Descuentos Admin", description = "Operaciones administrativas de cupones")
public class DescuentosControllerAdmin {

    private final DescuentosService descuentosService;

    @Operation(summary = "Crear cupón", description = "Crea un nuevo cupón de descuento (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cupón creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentosResponseDTO> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón: {}", request.getCode());
        DescuentosResponseDTO created = descuentosService.createCoupon(request);
        created.add(linkTo(methodOn(DescuentosControllerAdmin.class).createCoupon(null)).withSelfRel());
        created.add(linkTo(methodOn(DescuentosControllerAdmin.class).activate(created.getDiscountId())).withRel("activar"));
        created.add(linkTo(methodOn(DescuentosControllerAdmin.class).deactivate(created.getDiscountId())).withRel("desactivar"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Crear cupón para usuario", description = "Crea un cupón asignado a un usuario específico (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cupón creado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PostMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentosResponseDTO> createCouponForUser(@PathVariable String username,
                                                                     @Valid @RequestBody CreateCouponRequest request) {
        log.info("ADMIN - Creando cupón para usuario: {} con código: {}", username, request.getCode());
        DescuentosResponseDTO created = descuentosService.createCouponForUser(username, request);
        created.add(linkTo(methodOn(DescuentosControllerAdmin.class).createCouponForUser(username, null)).withSelfRel());
        created.add(linkTo(methodOn(DescuentosControllerAdmin.class).activate(created.getDiscountId())).withRel("activar"));
        created.add(linkTo(methodOn(DescuentosControllerAdmin.class).deactivate(created.getDiscountId())).withRel("desactivar"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Desactivar cupón", description = "Desactiva un cupón por su ID (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cupón desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        log.info("ADMIN - Desactivando cupón con id: {}", id);
        descuentosService.deactivateCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar cupón", description = "Activa un cupón por su ID (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón activado correctamente"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentosResponseDTO> activate(@PathVariable Long id) {
        log.info("ADMIN - Activando cupón con id: {}", id);
        descuentosService.activateCoupon(id);
        DescuentosResponseDTO coupon = descuentosService.getCouponByCode(
                descuentosService.findByIdOrThrow(id).getCode());
        coupon.add(linkTo(methodOn(DescuentosControllerAdmin.class).activate(id)).withSelfRel());
        coupon.add(linkTo(methodOn(DescuentosControllerAdmin.class).deactivate(id)).withRel("desactivar"));
        return ResponseEntity.ok(coupon);
    }

    @Operation(summary = "Verificar existencia de cupón", description = "Verifica si un cupón existe por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación exitosa")
    })
    @GetMapping("/exists/{id}")
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable Long id) {
        log.debug("Verificando existencia de cupón por ID: {}", id);
        boolean exists = descuentosService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}