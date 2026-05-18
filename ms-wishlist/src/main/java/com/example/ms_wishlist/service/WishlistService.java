package com.example.ms_wishlist.service;

import com.example.ms_wishlist.client.CatalogoClient;
import com.example.ms_wishlist.client.UserClient;
import com.example.ms_wishlist.dto.WishlistDTO;
import com.example.ms_wishlist.exception.custom.*;
import com.example.ms_wishlist.model.WishlistItem;
import com.example.ms_wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistRepository wishlistRepository;
    private final UserClient userClient;
    private final CatalogoClient catalogoClient;

    // Buscar item por username y productId - lanza excepción si no existe
    public WishlistItem findByUsernameAndProductIdOrThrow(String username, String productId) {
        log.debug("Buscando item en wishlist - Usuario: {}, Producto: {}", username, productId);
        return wishlistRepository.findByUsernameAndProductId(username, productId)
                .orElseThrow(() -> {
                    log.warn("Item no encontrado en wishlist - Usuario: {}, Producto: {}", username, productId);
                    return new WishlistItemNotFoundException("El producto no está en tu lista de deseados");
                });
    }

    // Agregar a wishlist
    public WishlistItem addToWishlist(String username, WishlistDTO dto) {
        log.info("Agregando producto a wishlist - Usuario: {}, Producto: {}", username, dto.getProductId());

        // Validar usuario
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe al agregar a wishlist: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // Validar producto
        if (!catalogoClient.productExists(dto.getProductId())) {
            log.warn("Producto no existe al agregar a wishlist - Usuario: {}, Producto: {}",
                    username, dto.getProductId());
            throw new ProductNotFoundException("Producto no existe con ID: " + dto.getProductId());
        }

        // Validar que no exista ya
        if (wishlistRepository.existsByUsernameAndProductId(username, dto.getProductId())) {
            log.warn("Producto ya existe en wishlist - Usuario: {}, Producto: {}",
                    username, dto.getProductId());
            throw new WishlistItemAlreadyExistsException("El producto ya está en tu lista de deseados");
        }

        WishlistItem item = WishlistItem.builder()
                .username(username)
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .productType(dto.getProductType())
                .productPrice(dto.getProductPrice())
                .imageUrl(dto.getImageUrl())
                .addedAt(LocalDateTime.now())
                .build();

        WishlistItem savedItem = wishlistRepository.save(item);
        log.info("Producto agregado a wishlist exitosamente - Usuario: {}, Producto: {}, ID: {}",
                username, dto.getProductId(), savedItem.getId());

        return savedItem;
    }

    // Obtener wishlist de un usuario
    public List<WishlistItem> getMyWishlist(String username) {
        log.debug("Obteniendo wishlist del usuario: {}", username);
        List<WishlistItem> wishlist = wishlistRepository.findByUsername(username);
        log.debug("Wishlist obtenida para usuario {} - Total items: {}", username, wishlist.size());
        return wishlist;
    }

    // Verificar si un producto está en wishlist
    public boolean isInWishlist(String username, String productId) {
        log.debug("Verificando si producto {} está en wishlist de: {}", productId, username);
        boolean exists = wishlistRepository.existsByUsernameAndProductId(username, productId);
        log.debug("Producto {} en wishlist de {}: {}", productId, username, exists);
        return exists;
    }

    // Contar items en wishlist
    public long countWishlistItems(String username) {
        log.debug("Contando items en wishlist de usuario: {}", username);
        long count = wishlistRepository.countByUsername(username);
        log.debug("Usuario {} tiene {} items en wishlist", username, count);
        return count;
    }

    // Eliminar de wishlist
    public void removeFromWishlist(String username, String productId) {
        log.info("Eliminando producto de wishlist - Usuario: {}, Producto: {}", username, productId);
        WishlistItem item = findByUsernameAndProductIdOrThrow(username, productId);
        wishlistRepository.delete(item);
        log.info("Producto eliminado de wishlist - Usuario: {}, Producto: {}", username, productId);
    }
}