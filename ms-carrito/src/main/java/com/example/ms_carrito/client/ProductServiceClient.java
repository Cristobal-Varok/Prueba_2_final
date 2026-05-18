package com.example.ms_carrito.client;

import com.example.ms_carrito.dto.ProductDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProductServiceClient {

    private final HttpServletRequest request;
    private final WebClient webClient;

    @Autowired
    public ProductServiceClient(WebClient.Builder webClientBuilder, HttpServletRequest request) {
        this.webClient = webClientBuilder
                .baseUrl("http://MS-PRODUCTOS")
                .build();
        this.request = request;
    }

    public ProductDTO getProductById(Long productId) {
        log.info("Consultando producto con id: {} en ms-products", productId);
        String token = this.request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            ProductDTO product = webClient.get()
                    .uri("/api/v1/productos/{id}", productId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            response -> Mono.error(new RuntimeException("Producto no encontrado")))
                    .bodyToMono(ProductDTO.class)
                    .block();

            log.debug("Producto obtenido: {}", product);
            System.out.println(product);
            return product;
        } catch (Exception e) {
            log.error("Error al consultar producto {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener información del producto: " + e.getMessage());
        }
    }
}