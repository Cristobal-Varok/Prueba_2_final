package com.example.ms_carrito.service;

import com.example.ms_carrito.client.ProductServiceClient;
import com.example.ms_carrito.client.UserClient;
import com.example.ms_carrito.dto.ProductDTO;
import com.example.ms_carrito.dto.request.AddItemRequest;
import com.example.ms_carrito.exception.custom.CartNotFoundException;
import com.example.ms_carrito.exception.custom.InsufficientStockException;
import com.example.ms_carrito.exception.custom.ItemNotFoundException;
import com.example.ms_carrito.exception.custom.UserNotFoundException;
import com.example.ms_carrito.model.Carrito;
import com.example.ms_carrito.model.CarritoItem;
import com.example.ms_carrito.model.CarritoStatus;
import com.example.ms_carrito.repository.CarritoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductServiceClient productClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private CarritoService carritoService;

    //getUserCart

    @Test
    void deberiaRetornarCarritoCuandoExiste() {
        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);

        when(carritoRepository.findByUserId(1L)).thenReturn(Optional.of(carrito));

        Carrito resultado = carritoService.getUserCart("testuser");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCartId());
        verify(carritoRepository).findByUserId(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoCarritoNoExiste() {
        when(carritoRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () ->
                carritoService.getUserCart("testuser"));
    }

    // ── addItem ──────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoUsuarioNoExiste() {
        when(userClient.userExists("noexiste")).thenReturn(false);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        assertThrows(UserNotFoundException.class, () ->
                carritoService.addItem("noexiste", request));
    }

    @Test
    void deberiaLanzarExcepcionCuandoStockInsuficiente() {
        when(userClient.userExists("testuser")).thenReturn(true);

        ProductDTO product = new ProductDTO();
        product.setNombre("Producto A");
        product.setPrecio(10.0);
        product.setStock(1); //stock menor a lo solicitado

        when(productClient.getProductById(1L)).thenReturn(product);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(5);

        assertThrows(InsufficientStockException.class, () ->
                carritoService.addItem("testuser", request));
    }

    @Test
    void deberiaAgregarItemNuevoAlCarrito() {
        when(userClient.userExists("testuser")).thenReturn(true);

        ProductDTO product = new ProductDTO();
        product.setNombre("Producto A");
        product.setPrecio(10.0);
        product.setStock(10);

        when(productClient.getProductById(1L)).thenReturn(product);

        Carrito carrito = Carrito.builder()
                .cartId(1L)
                .userId(1L)
                .status(CarritoStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserId(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        Carrito resultado = carritoService.addItem("testuser", request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getItems().size());
        verify(carritoRepository).save(any(Carrito.class));
    }

    // ── removeItem ───────────────────────────────────────────

    @Test
    void deberiaEliminarItemDelCarrito() {
        CarritoItem item = new CarritoItem();
        item.setProductId(1L);

        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);
        carrito.setItems(new ArrayList<>(java.util.List.of(item)));

        when(carritoRepository.findByUserId(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        Carrito resultado = carritoService.removeItem("testuser", 1L);

        assertTrue(resultado.getItems().isEmpty());
        verify(carritoRepository).save(any(Carrito.class));
    }

    // ── clearCart ────────────────────────────────────────────

    @Test
    void deberiaLimpiarTodosLosItemsDelCarrito() {
        CarritoItem item = new CarritoItem();
        item.setProductId(1L);

        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);
        carrito.setItems(new ArrayList<>(java.util.List.of(item)));

        when(carritoRepository.findByUserId(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.clearCart("testuser");

        assertTrue(carrito.getItems().isEmpty());
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoItemNoExisteAlEliminar() {
        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);
        carrito.setItems(new ArrayList<>());

        when(carritoRepository.findByUserId(1L)).thenReturn(Optional.of(carrito));

        assertThrows(ItemNotFoundException.class, () ->
                carritoService.removeItem("testuser", 99L));
    }

    @Test
    void deberiaActualizarCantidadDeItem() {
        CarritoItem item = new CarritoItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(10.0);

        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);
        carrito.setItems(new ArrayList<>(List.of(item)));

        ProductDTO product = new ProductDTO();
        product.setNombre("Producto A");
        product.setStock(10);
        product.setPrecio(10.0);

        when(carritoRepository.findByUserId(1L)).thenReturn(Optional.of(carrito));
        when(productClient.getProductById(1L)).thenReturn(product);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        Carrito resultado = carritoService.updateItemQuantity("testuser", 1L, 5);

        assertEquals(5, resultado.getItems().get(0).getQuantity());
        verify(carritoRepository).save(any(Carrito.class));
    }

}