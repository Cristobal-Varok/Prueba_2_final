package com.example.ms_carrito.controller;

import com.example.ms_carrito.dto.request.AddItemRequest;
import com.example.ms_carrito.dto.response.CarritoItemResponseDTO;
import com.example.ms_carrito.dto.response.CarritoResponseDTO;
import com.example.ms_carrito.model.Carrito;
import com.example.ms_carrito.model.CarritoItem;
import com.example.ms_carrito.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    // Obtener mi carrito
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CarritoResponseDTO> getMyCart(Authentication authentication) {
        String username = authentication.getName();
        log.info("Obteniendo carrito del usuario: {}", username);
        Carrito carrito = carritoService.getUserCart(username);
        return ResponseEntity.ok(mapToResponseDTO(carrito));
    }

    // Agregar item al carrito
    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CarritoResponseDTO> addItem(Authentication authentication,
                                                      @Valid @RequestBody AddItemRequest request) {
        String username = authentication.getName();
        log.info("Agregando item al carrito del usuario: {}, producto: {}", username, request.getProductId());
        Carrito updatedCarrito = carritoService.addItem(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(updatedCarrito));
    }

    // Eliminar item del carrito
    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CarritoResponseDTO> removeItem(Authentication authentication,
                                                         @PathVariable Long productId) {
        String username = authentication.getName();
        log.info("Eliminando producto {} del carrito de {}", productId, username);
        Carrito updatedCarrito = carritoService.removeItem(username, productId);
        return ResponseEntity.ok(mapToResponseDTO(updatedCarrito));
    }

    // Limpiar carrito
    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        String username = authentication.getName();
        log.info("Limpiando carrito del usuario: {}", username);
        carritoService.clearCart(username);
        return ResponseEntity.noContent().build();
    }

    // Actualizar cantidad de un item
    @PutMapping("/items/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CarritoResponseDTO> updateQuantity(Authentication authentication,
                                                             @PathVariable Long productId,
                                                             @RequestParam Integer quantity) {
        String username = authentication.getName();
        log.info("Actualizando cantidad del producto {} a {} para usuario {}", productId, quantity, username);
        Carrito updatedCarrito = carritoService.updateItemQuantity(username, productId, quantity);
        return ResponseEntity.ok(mapToResponseDTO(updatedCarrito));
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