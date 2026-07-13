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
import com.example.ms_carrito.security.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoAdminController.class)
@Import(SecurityConfig.class)
class CarritoAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaRetornarCarritoDeCualquierUsuario() throws Exception {
        Carrito carrito = new Carrito();
        carrito.setCartId(1L);
        carrito.setUserId(10L);
        carrito.setItems(List.of());

        when(carritoService.findByUserIdOrThrow(10L)).thenReturn(carrito);

        mockMvc.perform(get("/api/v1/carrito/admin/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaLimpiarCarritoDeCualquierUsuarioYPersistir() throws Exception {
        mockMvc.perform(delete("/api/v1/carrito/admin/user/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(carritoService, times(1)).clearCartByUserId(10L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaVerificarExistenciaDeCarrito() throws Exception {
        when(carritoService.cartExists(10L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/carrito/admin/exists/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deberiaRechazarAccesoDeUsuarioSinRolAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/carrito/admin/user/10"))
                .andExpect(status().isForbidden());
    }
}