package com.example.ms_wishlist;

import com.example.ms_wishlist.controller.WishlistController;
import com.example.ms_wishlist.service.WishlistService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WishlistControllerTest {

    @Mock private WishlistService wishlistService;
    @Mock private Authentication authentication;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new WishlistController(wishlistService)).build();
        when(authentication.getName()).thenReturn("javier");
    }

    @Test
    void testController_GetMyWishlist_HttpOk() throws Exception {
        when(wishlistService.getMyWishlist("javier")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/wishlist")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testController_IsInWishlist_HttpOk() throws Exception {
        when(wishlistService.isInWishlist("javier", "prod-123")).thenReturn(true);

        mockMvc.perform(get("/api/v1/wishlist/check/prod-123")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inWishlist").value(true));
    }

    @Test
    void testController_CountWishlist_HttpOk() throws Exception {
        when(wishlistService.countWishlistItems("javier")).thenReturn(5L);

        mockMvc.perform(get("/api/v1/wishlist/count")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(5));
    }

    @Test
    void testController_RemoveFromWishlist_HttpOk() throws Exception {
        doNothing().when(wishlistService).removeFromWishlist("javier", "prod-123");

        mockMvc.perform(delete("/api/v1/wishlist/prod-123")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}