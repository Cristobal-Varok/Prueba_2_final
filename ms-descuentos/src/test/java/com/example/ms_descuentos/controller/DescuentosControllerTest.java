package com.example.ms_descuentos.controller;

import com.example.ms_descuentos.dto.response.DescuentosResponseDTO;
import com.example.ms_descuentos.dto.response.DescuentosResult;
import com.example.ms_descuentos.model.DescuentosType;
import com.example.ms_descuentos.security.jwt.JwtService;
import com.example.ms_descuentos.service.CustomUserDetailsService;
import com.example.ms_descuentos.service.DescuentosService;
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

@WebMvcTest({DescuentosController.class, DescuentosControllerAdmin.class, DescuentosControllerUser.class})
class DescuentosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DescuentosService descuentosService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    //Helper

    private DescuentosResponseDTO buildDTO() {
        return DescuentosResponseDTO.builder()
                .discountId(1L)
                .code("VERANO2025")
                .discountType(DescuentosType.PERCENTAGE)
                .discountValue(10.0)
                .active(true)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(10))
                .minPurchaseAmount(0.0)
                .currentUses(0)
                .maxUses(100)
                .build();
    }

    //DescuentosControllerUser

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaListarTodosLosCupones() throws Exception {
        when(descuentosService.listAll()).thenReturn(List.of(buildDTO()));

        mockMvc.perform(get("/api/v1/discounts/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("VERANO2025"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaListarCuponesActivos() throws Exception {
        when(descuentosService.listActiveCoupons()).thenReturn(List.of(buildDTO()));

        mockMvc.perform(get("/api/v1/discounts/user/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("VERANO2025"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaObtenerCuponPorCodigo() throws Exception {
        when(descuentosService.getCouponByCode("VERANO2025")).thenReturn(buildDTO());

        mockMvc.perform(get("/api/v1/discounts/user/VERANO2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("VERANO2025"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.todos").exists());
    }

    //DescuentosController

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaValidarCupon() throws Exception {
        DescuentosResult result = new DescuentosResult(true, 10.0, "Cupón aplicado correctamente", "VERANO2025");
        when(descuentosService.validateCoupon(any())).thenReturn(result);

        mockMvc.perform(get("/api/v1/descuentos/validate/VERANO2025")
                        .param("cartTotal", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(10.0));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deberiaUsarCupon() throws Exception {
        DescuentosResult result = new DescuentosResult(true, 10.0, "Cupón aplicado correctamente", "VERANO2025");
        when(descuentosService.useCoupon(anyString(), anyDouble())).thenReturn(result);

        mockMvc.perform(post("/api/v1/descuentos/use")
                        .param("code", "VERANO2025")
                        .param("cartTotal", "100.0")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.couponCode").value("VERANO2025"));
    }

    //DescuentosControllerAdmin

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaCrearCupon() throws Exception {
        when(descuentosService.createCoupon(any())).thenReturn(buildDTO());

        String json = """
                {
                    "code": "VERANO2025",
                    "discountType": "PERCENTAGE",
                    "discountValue": 10.0,
                    "validFrom": "2025-01-01T00:00:00",
                    "validUntil": "2025-12-31T23:59:59",
                    "maxUses": 100,
                    "minPurchaseAmount": 0.0,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/descuentos/admin")
                        .contentType("application/json")
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("VERANO2025"))
                .andExpect(jsonPath("$._links.activar").exists())
                .andExpect(jsonPath("$._links.desactivar").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaDesactivarCupon() throws Exception {
        mockMvc.perform(delete("/api/v1/descuentos/admin/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deberiaVerificarExistenciaDeCupon() throws Exception {
        when(descuentosService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/descuentos/admin/exists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }
}