package com.example.ms_envios.controller;

import com.example.ms_envios.dto.CreateEnvioRequest;
import com.example.ms_envios.security.filter.JwtAuthFilter;
import com.example.ms_envios.security.jwt.JwtService;
import com.example.ms_envios.service.CustomUserDetailsService;
import com.example.ms_envios.service.EnviosService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnviosController.class)
@AutoConfigureMockMvc
class EnviosControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnviosService enviosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void createShipping_ShouldReturn401_WhenNoToken() throws Exception {
        CreateEnvioRequest request = new CreateEnvioRequest();
        request.setOrderId(1L);
        request.setAddress("Av. Siempre Viva 123");

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/envios/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByOrder_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/envios/order/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyShippings_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/envios/my-shippings"))
                .andExpect(status().isUnauthorized());
    }
}