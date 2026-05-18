package com.example.ms_carrito.service;

import com.example.ms_carrito.client.ProductServiceClient;
import com.example.ms_carrito.client.UserClient;
import com.example.ms_carrito.dto.ProductDTO;
import com.example.ms_carrito.dto.request.AddItemRequest;
import com.example.ms_carrito.exception.custom.*;
import com.example.ms_carrito.model.Carrito;
import com.example.ms_carrito.model.CarritoItem;
import com.example.ms_carrito.model.CarritoStatus;
import com.example.ms_carrito.repository.CarritoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductServiceClient productClient;
    private final UserClient userClient;

    public Carrito findByIdOrThrow(Long cartId) {
        log.debug("Buscando carrito por ID: {}", cartId);
        return carritoRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("Carrito no encontrado - ID: {}", cartId);
                    return new CartNotFoundException("Carrito no encontrado con ID: " + cartId);
                });
    }

    public Carrito findByUserIdOrThrow(Long Id) {
        log.debug("Buscando carrito por usuario ID: {}", Id);
        return carritoRepository.findByUserId(Id)
                .orElseThrow(() -> {
                    log.warn("Carrito no encontrado para usuario ID: {}", Id);
                    return new CartNotFoundException("Carrito no encontrado para el usuario: " + Id);
                });
    }

    public boolean cartExists(Long Id) {
        log.debug("Verificando existencia de carrito para usuario: {}", Id);
        return carritoRepository.existsByUserId(Id);
    }

    private Carrito getOrCreateCart(Long userId) {
        return carritoRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creando nuevo carrito para usuario {}", userId);
                    Carrito newCarrito = Carrito.builder()
                            .userId(userId)
                            .status(CarritoStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .items(new java.util.ArrayList<>())
                            .build();
                    return carritoRepository.save(newCarrito);
                });
    }

    @Transactional
    public Carrito addItem(String username, AddItemRequest request) {
        log.info("Agregando item al carrito del usuario: {}, producto: {}", username, request.getProductId());

        // Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // Validar producto en ms_products
        ProductDTO product;
        try {
            product = productClient.getProductById(request.getProductId());
        } catch (Exception e) {
            log.error("Error al obtener producto {}: {}", request.getProductId(), e.getMessage());
            throw new ProductNotFoundException("Producto no encontrado con ID: " + request.getProductId());
        }

        if (product.getStock() < request.getQuantity()) {
            log.warn("Stock insuficiente para producto {}: stock={}, solicitado={}",
                    product.getNombre(), product.getStock(), request.getQuantity());
            throw new InsufficientStockException("Stock insuficiente para el producto " + product.getNombre() +
                    ". Disponible: " + product.getStock());
        }

        // Obtener o crear carrito
        Carrito carrito = getOrCreateCart(getUserIdFromUsername(username));

        // Buscar si ya existe el item
        CarritoItem existingItem = carrito.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException("Stock insuficiente para incrementar cantidad del producto " + product.getNombre());
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setSubtotal(existingItem.getQuantity() * existingItem.getUnitPrice());
            log.debug("Cantidad actualizada para producto {}: nueva cantidad {}", product.getNombre(), newQuantity);
        } else {
            CarritoItem newItem = CarritoItem.builder()
                    .cart(carrito)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrecio())
                    .subtotal(product.getPrecio() * request.getQuantity())
                    .build();
            System.out.println(newItem);
            carrito.getItems().add(newItem);
            log.debug("Nuevo item agregado: {} x {}", product.getNombre(), request.getQuantity());
        }

        Carrito saved = carritoRepository.save(carrito);
        log.info("Carrito actualizado para usuario: {}", username);
        return saved;
    }

    @Transactional
    public Carrito removeItem(String username, Long productId) {
        log.info("Eliminando producto {} del carrito del usuario: {}", productId, username);
        Long userId = getUserIdFromUsername(username);
        Carrito carrito = findByUserIdOrThrow(userId);

        boolean removed = carrito.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            log.warn("Producto {} no encontrado en el carrito del usuario {}", productId, username);
            throw new ItemNotFoundException("Producto no encontrado en el carrito");
        }

        log.debug("Producto eliminado del carrito");
        return carritoRepository.save(carrito);
    }

    @Transactional
    public void clearCart(String username) {
        log.info("Limpiando carrito del usuario: {}", username);
        Long userId = getUserIdFromUsername(username);
        Carrito carrito = findByUserIdOrThrow(userId);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
        log.info("Carrito limpiado para usuario: {}", username);
    }

    public Carrito getUserCart(String username) {
        log.debug("Obteniendo carrito del usuario: {}", username);
        Long userId = getUserIdFromUsername(username);
        return findByUserIdOrThrow(userId);
    }

    @Transactional
    public Carrito updateItemQuantity(String username, Long productId, Integer newQuantity) {
        log.info("Actualizando cantidad para producto {} del usuario {} a {}", productId, username, newQuantity);
        Long userId = getUserIdFromUsername(username);
        Carrito carrito = findByUserIdOrThrow(userId);

        CarritoItem item = carrito.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Producto no encontrado en el carrito"));

        if (newQuantity <= 0) {
            carrito.getItems().remove(item);
            log.debug("Cantidad <=0, item eliminado");
        } else {
            // Validar stock nuevamente
            ProductDTO product;
            try {
                product = productClient.getProductById(productId);
            } catch (Exception e) {
                throw new ProductNotFoundException("Producto no encontrado con ID: " + productId);
            }

            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException("Stock insuficiente para el producto " + product.getNombre());
            }
            item.setQuantity(newQuantity);
            item.setSubtotal(item.getUnitPrice() * newQuantity);
            log.debug("Cantidad actualizada a {}", newQuantity);
        }
        return carritoRepository.save(carrito);
    }

    // Método temporal para obtener userId desde username
    private Long getUserIdFromUsername(String username) {
        // TODO: Llamar a ms_users para obtener el ID real
        // Por ahora, retornamos un ID fijo para pruebas
        return 1L;
    }
}