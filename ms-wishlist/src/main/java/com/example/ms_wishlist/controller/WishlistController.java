package com.example.ms_wishlist.controller;

import com.example.ms_wishlist.dto.WishlistDTO;
import com.example.ms_wishlist.model.WishlistItem;
import com.example.ms_wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);

    private final WishlistService wishlistService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addToWishlist(Authentication authentication, @Valid @RequestBody WishlistDTO dto) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de agregar a wishlist - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Agregando producto a wishlist - Usuario: {}, Producto: {}, Tipo: {}",
                username, dto.getProductId(), dto.getProductType());

        WishlistItem item = wishlistService.addToWishlist(username, dto);

        log.info("Producto agregado a wishlist exitosamente - Usuario: {}, Producto: {}, ID: {}",
                username, dto.getProductId(), item.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Producto agregado a tu lista de deseados",
                "item", Map.of(
                        "id", item.getId(),
                        "productId", item.getProductId(),
                        "productName", item.getProductName(),
                        "productType", item.getProductType(),
                        "productPrice", item.getProductPrice(),
                        "imageUrl", item.getImageUrl(),
                        "addedAt", item.getAddedAt()
                )
        ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMyWishlist(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener wishlist - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Obteniendo wishlist del usuario: {}", username);

        List<WishlistItem> wishlist = wishlistService.getMyWishlist(username);

        log.debug("Wishlist obtenida para usuario {} - Total items: {}", username, wishlist.size());

        return ResponseEntity.ok(Map.of(
                "wishlist", wishlist,
                "totalItems", wishlist.size()
        ));
    }

    @GetMapping("/check/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> isInWishlist(Authentication authentication, @PathVariable String productId) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de verificar producto en wishlist - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Verificando si producto {} está en wishlist de usuario: {}", productId, username);

        boolean exists = wishlistService.isInWishlist(username, productId);

        log.debug("Producto {} en wishlist de {}: {}", productId, username, exists);

        return ResponseEntity.ok(Map.of("inWishlist", exists));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> countWishlist(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de contar wishlist - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Contando items en wishlist de usuario: {}", username);

        long count = wishlistService.countWishlistItems(username);

        log.debug("Usuario {} tiene {} items en wishlist", username, count);

        return ResponseEntity.ok(Map.of("totalItems", count));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> removeFromWishlist(Authentication authentication, @PathVariable String productId) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de eliminar de wishlist - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.warn("Eliminando producto de wishlist - Usuario: {}, Producto: {}", username, productId);

        wishlistService.removeFromWishlist(username, productId);

        log.info("Producto {} eliminado de wishlist de usuario: {}", productId, username);

        return ResponseEntity.ok(Map.of("message", "Producto eliminado de tu lista de deseados"));
    }
}