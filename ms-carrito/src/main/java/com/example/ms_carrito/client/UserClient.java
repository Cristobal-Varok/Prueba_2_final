package com.example.ms_carrito.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class UserClient {

    private final WebClient webClient;

    @Autowired
    public UserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://MS-USERS")
                .build();
    }

    public Long getUserId(String username) {
        log.debug("Consultando ID del usuario en ms_users: {}", username);
        try {
            Map response = webClient.get()
                    .uri("/api/v1/users/username/{username}", username) // Asegúrate que esta sea la ruta correcta en tu ms-users
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("id")) {
                Object idObj = response.get("id");
                return Long.valueOf(idObj.toString());
            }
            return null;
        } catch (Exception e) {
            log.error("Error obteniendo ID del usuario '{}': {}", username, e.getMessage());
            return null;
        }
    }

    public boolean userExists(String username) {
        log.debug("Consultando existencia de usuario en ms_users: {}", username);
        try {
            Map response = webClient.get()
                    .uri("/api/v1/auth/user-exists/{username}", username)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            boolean exists = response != null && (Boolean) response.getOrDefault("exists", false);
            log.debug("Usuario {} - Existe: {}", username, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error validando usuario '{}' con ms_users: {}", username, e.getMessage(), e);
            return false;
        }
    }
}