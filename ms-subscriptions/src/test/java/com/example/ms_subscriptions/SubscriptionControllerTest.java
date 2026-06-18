package com.example.ms_subscriptions;

import com.example.ms_subscriptions.controller.SubsciptionControllerAdmin;
import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.model.SubscriptionType;
import com.example.ms_subscriptions.service.SubscriptionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SubsciptionControllerAdminTest {

    @Mock private SubscriptionService subscriptionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SubsciptionControllerAdmin controller = new SubsciptionControllerAdmin(subscriptionService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetAllSubscriptions_HttpOk() throws Exception {
        Subscription s = mock(Subscription.class);
        when(s.getId()).thenReturn(1L);
        when(s.getUsername()).thenReturn("admin");
        when(s.getType()).thenReturn(SubscriptionType.PREMIUM);
        when(s.getActive()).thenReturn(true);

        when(subscriptionService.getAllActiveSubscriptions()).thenReturn(Arrays.asList(s));

        mockMvc.perform(get("/api/v1/subscriptions/admin/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSubscriptionsByType_HttpOk() throws Exception {
        when(subscriptionService.getSubscriptionsByType(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/subscriptions/admin/type/VIP")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetExpiringSubscriptions_HttpOk() throws Exception {
        when(subscriptionService.getExpiringSubscriptions()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/subscriptions/admin/expiring")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserSubscription_Found() throws Exception {
        Subscription s = mock(Subscription.class);
        when(s.getId()).thenReturn(2L);
        when(s.getUsername()).thenReturn("javier");
        when(s.getType()).thenReturn(SubscriptionType.BASICA);
        when(s.getActive()).thenReturn(true);

        when(subscriptionService.getSubscriptionByUsername(anyString())).thenReturn(Optional.of(s));

        mockMvc.perform(get("/api/v1/subscriptions/admin/user/javier")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserSubscription_NotFound() throws Exception {
        when(subscriptionService.getSubscriptionByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/subscriptions/admin/user/desconocido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}