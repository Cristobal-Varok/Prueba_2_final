package com.example.ms_reviews.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Component
public class CatalogoClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogoClient.class);

    private final WebClient webClient;

    @Autowired
    public CatalogoClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ms-productos")
                .build();
    }


    private String getAuthToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getHeader("Authorization");
        }
        return null;
    }

    public boolean productExists(String productId) {
        log.debug("Consultando existencia de producto en catálogo: {}", productId);
        try {
            String token = getAuthToken();

            // Construir la petición
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri("/api/v1/productos/exists/{id}", productId);

            // Agregar el token si existe
            if (token != null && !token.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, token);
                log.debug("Token agregado a la petición");
            } else {
                log.warn("No se encontró token para enviar a ms-productos");
            }

            Map response = request
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            boolean exists = response != null && (Boolean) response.getOrDefault("exists", false);
            log.debug("Producto {} - Existe: {}", productId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error validando producto '{}' con catálogo: {}", productId, e.getMessage(), e);
            return false;
        }
    }
}