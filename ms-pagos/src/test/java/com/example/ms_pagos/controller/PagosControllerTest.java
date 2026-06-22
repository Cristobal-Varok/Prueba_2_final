package com.example.ms_pagos.controller;

import com.example.ms_pagos.dto.response.PagosResponseDTO;
import com.example.ms_pagos.model.PagosMethod;
import com.example.ms_pagos.model.PagosStatus;
import com.example.ms_pagos.security.jwt.JwtService;
import com.example.ms_pagos.service.CustomUserDetailsService;
import com.example.ms_pagos.service.PagosService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PagosController.class, PagosAdminController.class})
class PagosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagosService pagosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    //Helper

    private PagosResponseDTO buildDTO() {
        return PagosResponseDTO.builder()
                .paymentId(1L)
                .orderId(10L)
                .userId(1L)
                .amount(99.99)
                .method(PagosMethod.CREDIT_CARD)
                .status(PagosStatus.COMPLETED)
                .transactionId("TXN-123")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    //PagosController

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaProcesarPago() throws Exception {
        when(pagosService.processPayment(anyString(), any())).thenReturn(buildDTO());

        String json = """
                {
                    "orderId": 10,
                    "amount": 99.99,
                    "method": "CREDIT_CARD"
                }
                """;

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType("application/json")
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$._links.detalle").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaObtenerPagoPorId() throws Exception {
        when(pagosService.getPaymentById(1L)).thenReturn(buildDTO());

        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaObtenerPagosPorOrden() throws Exception {
        when(pagosService.getPaymentsByOrder(10L)).thenReturn(List.of(buildDTO()));

        mockMvc.perform(get("/api/v1/pagos/order/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(10))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    //PagosAdminController
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaObtenerPagosPorUsuario() throws Exception {
        when(pagosService.getPaymentsByUser(1L)).thenReturn(List.of(buildDTO()));

        mockMvc.perform(get("/api/v1/pagos/admin/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaVerificarExistenciaDePagoPorOrden() throws Exception {
        when(pagosService.paymentExistsByOrder(10L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/pagos/admin/exists/order/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}