package com.example.ms_tickets.client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class CatalogoClient {

    private final WebClient webClient;

    @Autowired
    public CatalogoClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ms-productos")  // Nombre del servicio en Eureka
                .build();
    }

    public boolean productExists(String productId) {
        try {
            Map response = webClient.get()
                    .uri("/api/v1/productos/exists/{id}", productId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null && (Boolean) response.getOrDefault("exists", false);
        } catch (Exception e) {
            System.out.println("Error validando producto: " + e.getMessage());
            return false;
        }
    }
}
