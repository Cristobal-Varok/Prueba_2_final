package com.example.ms_wishlist.controller;

import com.example.ms_wishlist.dto.WishlistDTO;
import com.example.ms_wishlist.dto.WishlistResponseDTO;
import com.example.ms_wishlist.model.WishlistItem;
import com.example.ms_wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist", description = "Endpoints para gestionar lista de deseos de usuarios autenticados")
public class WishlistController {

    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);

    private final WishlistService wishlistService;

    @Operation(summary = "Agregar producto a wishlist", description = "Agrega un producto (juego o cómic) a la lista de deseos del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario o producto no existe"),
            @ApiResponse(responseCode = "409", description = "Producto ya existe en wishlist")
    })
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

        // Convertir a DTO con enlaces
        WishlistResponseDTO responseDTO = WishlistResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productType(item.getProductType())
                .productPrice(item.getProductPrice())
                .imageUrl(item.getImageUrl())
                .addedAt(item.getAddedAt())
                .build();

        responseDTO.add(linkTo(methodOn(WishlistController.class).getMyWishlist(authentication)).withRel("myWishlist"));
        responseDTO.add(linkTo(methodOn(WishlistController.class).removeFromWishlist(authentication, item.getProductId())).withRel("remove"));
        responseDTO.add(linkTo(methodOn(WishlistController.class).isInWishlist(authentication, item.getProductId())).withRel("check"));

        log.info("Producto agregado a wishlist exitosamente - Usuario: {}, Producto: {}, ID: {}",
                username, dto.getProductId(), item.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Producto agregado a tu lista de deseados",
                "item", responseDTO
        ));
    }

    @Operation(summary = "Obtener wishlist del usuario", description = "Devuelve todos los productos en la lista de deseos del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
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

        List<WishlistResponseDTO> wishlistDTOs = wishlist.stream().map(item -> {
            WishlistResponseDTO dto = WishlistResponseDTO.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .productType(item.getProductType())
                    .productPrice(item.getProductPrice())
                    .imageUrl(item.getImageUrl())
                    .addedAt(item.getAddedAt())
                    .build();

            dto.add(linkTo(methodOn(WishlistController.class).getMyWishlist(authentication)).withSelfRel());
            dto.add(linkTo(methodOn(WishlistController.class).removeFromWishlist(authentication, item.getProductId())).withRel("remove"));
            dto.add(linkTo(methodOn(WishlistController.class).isInWishlist(authentication, item.getProductId())).withRel("check"));

            return dto;
        }).collect(Collectors.toList());

        log.debug("Wishlist obtenida para usuario {} - Total items: {}", username, wishlistDTOs.size());

        return ResponseEntity.ok(Map.of(
                "wishlist", wishlistDTOs,
                "totalItems", wishlistDTOs.size(),
                "_links", Map.of("self", linkTo(methodOn(WishlistController.class).getMyWishlist(authentication)).withSelfRel())
        ));
    }

    @Operation(summary = "Verificar si producto está en wishlist", description = "Comprueba si un producto específico está en la lista de deseos del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
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

        return ResponseEntity.ok(Map.of(
                "inWishlist", exists,
                "productId", productId,
                "_links", Map.of(
                        "add", linkTo(methodOn(WishlistController.class).addToWishlist(authentication, null)).withRel("add"),
                        "remove", linkTo(methodOn(WishlistController.class).removeFromWishlist(authentication, productId)).withRel("remove"),
                        "myWishlist", linkTo(methodOn(WishlistController.class).getMyWishlist(authentication)).withRel("myWishlist")
                )
        ));
    }

    @Operation(summary = "Contar items en wishlist", description = "Devuelve la cantidad de productos en la lista de deseos del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conteo exitoso"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
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

        return ResponseEntity.ok(Map.of(
                "totalItems", count,
                "_links", Map.of(
                        "myWishlist", linkTo(methodOn(WishlistController.class).getMyWishlist(authentication)).withRel("myWishlist"),
                        "add", linkTo(methodOn(WishlistController.class).addToWishlist(authentication, null)).withRel("add")
                )
        ));
    }

    @Operation(summary = "Eliminar producto de wishlist", description = "Remueve un producto específico de la lista de deseos del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en wishlist")
    })
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

        return ResponseEntity.ok(Map.of(
                "message", "Producto eliminado de tu lista de deseados",
                "productId", productId,
                "_links", Map.of(
                        "myWishlist", linkTo(methodOn(WishlistController.class).getMyWishlist(authentication)).withRel("myWishlist"),
                        "add", linkTo(methodOn(WishlistController.class).addToWishlist(authentication, null)).withRel("add"),
                        "check", linkTo(methodOn(WishlistController.class).isInWishlist(authentication, productId)).withRel("check")
                )
        ));
    }
}