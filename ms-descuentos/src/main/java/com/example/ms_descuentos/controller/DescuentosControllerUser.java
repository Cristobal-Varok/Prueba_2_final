package com.example.ms_descuentos.controller;

import com.example.ms_descuentos.dto.response.DescuentosResponseDTO;
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

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/discounts/user")
@RequiredArgsConstructor
@Tag(name = "Descuentos Usuario", description = "Operaciones de consulta de cupones para usuarios")
public class DescuentosControllerUser {

    private final DescuentosService descuentosService;

    @Operation(summary = "Listar todos los cupones", description = "Retorna todos los cupones disponibles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<DescuentosResponseDTO>> listAll() {
        log.info("Listando todos los cupones");
        List<DescuentosResponseDTO> cupones = descuentosService.listAll();
        cupones.forEach(c ->
                c.add(linkTo(methodOn(DescuentosControllerUser.class).getByCode(c.getCode())).withSelfRel())
        );
        return ResponseEntity.ok(cupones);
    }

    @Operation(summary = "Listar cupones activos", description = "Retorna solo los cupones activos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<DescuentosResponseDTO>> listActive() {
        log.info("Listando cupones activos");
        List<DescuentosResponseDTO> cupones = descuentosService.listActiveCoupons();
        cupones.forEach(c ->
                c.add(linkTo(methodOn(DescuentosControllerUser.class).getByCode(c.getCode())).withSelfRel())
        );
        return ResponseEntity.ok(cupones);
    }

    @Operation(summary = "Obtener cupón por código", description = "Retorna un cupón por su código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupón encontrado"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<DescuentosResponseDTO> getByCode(@PathVariable String code) {
        log.info("Obteniendo cupón por código: {}", code);
        DescuentosResponseDTO coupon = descuentosService.getCouponByCode(code);
        coupon.add(linkTo(methodOn(DescuentosControllerUser.class).getByCode(code)).withSelfRel());
        coupon.add(linkTo(methodOn(DescuentosControllerUser.class).listAll()).withRel("todos"));
        coupon.add(linkTo(methodOn(DescuentosControllerUser.class).listActive()).withRel("activos"));
        return ResponseEntity.ok(coupon);
    }

    @Operation(summary = "Verificar existencia de cupón por código", description = "Verifica si un cupón existe por su código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/exists/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Map<String, Boolean>> couponExists(@PathVariable String code) {
        log.debug("Verificando existencia de cupón por código: {}", code);
        boolean exists = descuentosService.couponExists(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}