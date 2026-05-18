package com.example.ms_tickets.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class UserClient {

    private final WebClient webClient;

    @Autowired
    public UserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ms-users")  // Nombre del servicio en Eureka
                .build();
    }

    public boolean userExists(String username) {
        try {
            Map response = webClient.get()
                    .uri("/api/v1/auth/user-exists/{username}", username)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null && (Boolean) response.getOrDefault("exists", false);
        } catch (Exception e) {
            System.out.println("❌ Error validando usuario: " + e.getMessage());
            return false;
        }
    }
}
