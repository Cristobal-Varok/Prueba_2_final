package com.example.ms_envios.controller;

import com.example.ms_envios.model.EnviosStatus;
import com.example.ms_envios.security.filter.JwtAuthFilter;
import com.example.ms_envios.security.jwt.JwtService;
import com.example.ms_envios.service.CustomUserDetailsService;
import com.example.ms_envios.service.EnviosService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnviosAdminController.class)
@AutoConfigureMockMvc
class EnviosAdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnviosService enviosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getUserShippings_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/envios/admin/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByStatus_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/envios/admin/status/PENDING"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shippingExistsByOrder_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/envios/admin/exists/order/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateStatus_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(put("/api/v1/envios/admin/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isUnauthorized());
    }
}