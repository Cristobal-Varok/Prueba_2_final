package com.example.ms_wishlist.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class CatalogoClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogoClient.class);

    private final WebClient webClient;

    @Autowired
    public CatalogoClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://Catalago")
                .build();
    }

    public boolean productExists(String productId) {
        log.debug("Consultando existencia de producto en catálogo: {}", productId);
        try {
            Map response = webClient.get()
                    .uri("/api/productos/exists/{id}", productId)
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