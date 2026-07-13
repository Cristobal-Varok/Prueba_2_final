package com.example.ms_carrito.controller;

import com.example.ms_carrito.dto.response.CarritoItemResponseDTO;
import com.example.ms_carrito.dto.response.CarritoResponseDTO;
import com.example.ms_carrito.model.Carrito;
import com.example.ms_carrito.model.CarritoItem;
import com.example.ms_carrito.service.CarritoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/carrito/admin")
@RequiredArgsConstructor
public class CarritoAdminController {

    private final CarritoService carritoService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarritoResponseDTO> getUserCart(@PathVariable("userId") Long Id) {
        log.info("ADMIN - Obteniendo carrito del usuario {}", Id);
        Carrito carrito = carritoService.findByUserIdOrThrow(Id);
        return ResponseEntity.ok(mapToResponseDTO(carrito));
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearUserCart(@PathVariable("userId") Long Id) {
        log.info("ADMIN - Limpiando carrito del usuario {}", Id);
        carritoService.clearCartByUserId(Id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> cartExists(@PathVariable("userId") Long Id) {
        log.debug("ADMIN - Verificando existencia de carrito para usuario: {}", Id);
        boolean exists = carritoService.cartExists(Id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    private CarritoResponseDTO mapToResponseDTO(Carrito carrito) {
        return CarritoResponseDTO.builder()
                .cartId(carrito.getCartId())
                .userId(carrito.getUserId())
                .items(carrito.getItems().stream()
                        .map(this::mapToItemDTO)
                        .collect(Collectors.toList()))
                .total(carrito.getTotal())
                .createdAt(carrito.getCreatedAt())
                .updatedAt(carrito.getUpdatedAt())
                .build();
    }

    private CarritoItemResponseDTO mapToItemDTO(CarritoItem item) {
        return CarritoItemResponseDTO.builder()
                .productId(item.getProductId())
                .productName("Desconocido")
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}