package com.example.ms_envios.controller;

import com.example.ms_envios.dto.response.EnvioResponseDTO;
import com.example.ms_envios.model.EnviosStatus;
import com.example.ms_envios.security.filter.JwtAuthFilter;
import com.example.ms_envios.security.jwt.JwtService;
import com.example.ms_envios.service.CustomUserDetailsService;
import com.example.ms_envios.service.EnviosService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnviosAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnviosAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnviosService enviosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private EnvioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = EnvioResponseDTO.builder()
                .shippingId(1L)
                .orderId(1L)
                .userId(101L)
                .address("Av. Siempre Viva 123")
                .status(EnviosStatus.PENDING)
                .trackingNumber("TRK12345678")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserShippings_ShouldReturn200() throws Exception {
        when(enviosService.getByUser(101L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/envios/admin/user/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shippingId").value(1))
                .andExpect(jsonPath("$[0]._links.self.href").exists())
                .andExpect(jsonPath("$[0]._links.userShippings.href").exists());

        verify(enviosService).getByUser(101L);
    }

    @Test
    void getByStatus_ShouldReturn200() throws Exception {
        when(enviosService.getByStatus(EnviosStatus.PENDING)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/envios/admin/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shippingId").value(1))
                .andExpect(jsonPath("$[0]._links.self.href").exists());

        verify(enviosService).getByStatus(EnviosStatus.PENDING);
    }

    @Test
    void shippingExistsByOrder_ShouldReturnTrue() throws Exception {
        when(enviosService.shippingExistsByOrder(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/envios/admin/exists/order/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(enviosService).shippingExistsByOrder(1L);
    }

    @Test
    void shippingExistsByOrder_ShouldReturnFalse() throws Exception {
        when(enviosService.shippingExistsByOrder(99L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/envios/admin/exists/order/99"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(enviosService).shippingExistsByOrder(99L);
    }

    @Test
    void updateStatus_ShouldReturn200() throws Exception {
        EnvioResponseDTO updatedDTO = EnvioResponseDTO.builder()
                .shippingId(1L)
                .orderId(1L)
                .userId(101L)
                .address("Av. Siempre Viva 123")
                .status(EnviosStatus.SHIPPED)
                .trackingNumber("TRK12345678")
                .createdAt(LocalDateTime.now())
                .shippedAt(LocalDateTime.now())
                .estimatedDelivery(LocalDateTime.now().plusDays(5))
                .build();

        when(enviosService.updateStatus(1L, EnviosStatus.SHIPPED)).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/v1/envios/admin/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingId").value(1))
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(enviosService).updateStatus(1L, EnviosStatus.SHIPPED);
    }
}