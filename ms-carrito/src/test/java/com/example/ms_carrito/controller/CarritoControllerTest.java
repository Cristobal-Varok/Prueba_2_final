package com.example.ms_carrito.controller;

import com.example.ms_carrito.model.Carrito;
import com.example.ms_carrito.security.jwt.JwtService;
import com.example.ms_carrito.service.CarritoService;
import com.example.ms_carrito.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoController.class)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaRetornarCarritoDelUsuario() throws Exception {
        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(10L);
        carrito.setItems(List.of());

        when(carritoService.getUserCart(anyString()))
                .thenReturn(carrito);

        mockMvc.perform(get("/api/v1/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.total").value(0.0))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaAgregarItemAlCarrito() throws Exception {
        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);
        carrito.setItems(List.of());

        when(carritoService.addItem(anyString(), any()))
                .thenReturn(carrito);

        String json = """
                {
                    "productId": 1,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/v1/carrito/items")
                        .contentType("application/json")
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$._links.mi-carrito").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaEliminarItemDelCarrito() throws Exception {
        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);
        carrito.setItems(List.of());

        when(carritoService.removeItem(anyString(), anyLong()))
                .thenReturn(carrito);

        mockMvc.perform(delete("/api/v1/carrito/items/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$._links.mi-carrito").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaVaciarCarrito() throws Exception {
        mockMvc.perform(delete("/api/v1/carrito")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaActualizarCantidadDeItem() throws Exception {
        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(1L);
        carrito.setItems(List.of());

        when(carritoService.updateItemQuantity(anyString(), anyLong(), anyInt()))
                .thenReturn(carrito);

        mockMvc.perform(put("/api/v1/carrito/items/1")
                        .param("quantity", "3")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$._links.self").exists());
    }
}