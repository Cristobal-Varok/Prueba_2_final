package com.example.ms_carrito.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsServiceTest {

    private final CustomUserDetailsService service = new CustomUserDetailsService();

    @Test
    void deberiaCargarUserDetailsConElUsernameDado() {
        UserDetails userDetails = service.loadUserByUsername("testuser");

        assertEquals("testuser", userDetails.getUsername());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }
}