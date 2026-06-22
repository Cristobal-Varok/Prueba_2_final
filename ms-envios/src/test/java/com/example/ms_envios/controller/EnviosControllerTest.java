package com.example.ms_envios.controller;

import com.example.ms_envios.dto.CreateEnvioRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnviosController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnviosControllerTest {

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
    private CreateEnvioRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateEnvioRequest();
        request.setOrderId(1L);
        request.setAddress("Av. Siempre Viva 123");

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
    void createShipping_ShouldReturn201_WhenValid() throws Exception {
        when(enviosService.createShipping(anyString(), any(CreateEnvioRequest.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingId").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.order.href").exists())
                .andExpect(jsonPath("$._links.myShippings.href").exists())
                .andExpect(jsonPath("$._links.updateStatus.href").exists());

        verify(enviosService).createShipping(anyString(), any(CreateEnvioRequest.class));
    }

    @Test
    void createShipping_ShouldReturn400_WhenInvalidData() throws Exception {
        CreateEnvioRequest invalidRequest = new CreateEnvioRequest();

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(enviosService, never()).createShipping(anyString(), any());
    }

    @Test
    void getById_ShouldReturn200_WhenExists() throws Exception {
        when(enviosService.getShipping(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingId").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.order.href").exists())
                .andExpect(jsonPath("$._links.updateStatus.href").exists());

        verify(enviosService).getShipping(1L);
    }

    @Test
    void getByOrder_ShouldReturn200() throws Exception {
        when(enviosService.getByOrder(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/envios/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shippingId").value(1))
                .andExpect(jsonPath("$[0]._links.self.href").exists());

        verify(enviosService).getByOrder(1L);
    }

    @Test
    void getMyShippings_ShouldReturn200() throws Exception {
        when(enviosService.getByUser(anyLong())).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/envios/my-shippings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shippingId").value(1))
                .andExpect(jsonPath("$[0]._links.self.href").exists())
                .andExpect(jsonPath("$[0]._links.myShippings.href").exists());

        verify(enviosService).getByUser(anyLong());
    }
}