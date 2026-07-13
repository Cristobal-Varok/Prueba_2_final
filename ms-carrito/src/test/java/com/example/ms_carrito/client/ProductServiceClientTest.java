package com.example.ms_carrito.client;

import com.example.ms_carrito.dto.ProductDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceClientTest {

    @Test
    @SuppressWarnings("unchecked")
    void deberiaObtenerProductoCorrectamente() {
        WebClient webClient = mock(WebClient.class);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");

        ProductDTO productoEsperado = new ProductDTO();
        productoEsperado.setId(1L);

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString(), eq(1L));
        doReturn(headersSpec).when(headersSpec).header(anyString(), any(String[].class));
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(Predicate.class), any());
        doReturn(Mono.just(productoEsperado)).when(responseSpec).bodyToMono(ProductDTO.class);

        ProductServiceClient client = new ProductServiceClient(builder, request);
        ProductDTO resultado = client.getProductById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deberiaLanzarExcepcionCuandoFallaLaConsulta() {
        WebClient webClient = mock(WebClient.class);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString(), eq(99L));
        doReturn(headersSpec).when(headersSpec).header(anyString(), any(String[].class));
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(Predicate.class), any());
        doReturn(Mono.error(new RuntimeException("Producto no encontrado"))).when(responseSpec).bodyToMono(ProductDTO.class);

        ProductServiceClient client = new ProductServiceClient(builder, request);

        assertThrows(RuntimeException.class, () -> client.getProductById(99L));
    }
}