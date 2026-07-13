package com.example.ms_carrito.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserClientTest {

    @Test
    void deberiaRetornarTrueCuandoUsuarioExiste() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        when(webClient.get()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .bodyToMono(Map.class)
                .block())
                .thenReturn(Map.of("exists", true));

        UserClient client = new UserClient(builder);

        assertTrue(client.userExists("testuser"));
    }

    @Test
    void deberiaRetornarFalseCuandoUsuarioNoExiste() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        when(webClient.get()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .bodyToMono(Map.class)
                .block())
                .thenReturn(Map.of("exists", false));

        UserClient client = new UserClient(builder);

        assertFalse(client.userExists("noexiste"));
    }

    @Test
    void deberiaRetornarFalseCuandoHayErrorDeConexion() {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        when(webClient.get()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .bodyToMono(Map.class)
                .block())
                .thenThrow(new RuntimeException("Conexión rechazada"));

        UserClient client = new UserClient(builder);

        assertFalse(client.userExists("testuser"));
    }
}