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
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final WebClient webClient;

    @Autowired
    public UserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ms-users")
                .build();
    }

    // Obtener el token del request actual
    private String getAuthToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getHeader("Authorization");
        }
        return null;
    }

    public boolean userExists(String username) {
        log.debug("Consultando existencia de usuario en ms-users: {}", username);
        try {
            String token = getAuthToken();

            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri("/api/v1/auth/user-exists/{username}", username);

            // Agregar el token si existe
            if (token != null && !token.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, token);
                log.debug("Token agregado a la petición a ms-users");
            } else {
                log.warn("No se encontró token para enviar a ms-users");
            }

            Map response = request
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            boolean exists = response != null && (Boolean) response.getOrDefault("exists", false);
            log.debug("Usuario {} - Existe: {}", username, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error validando usuario '{}' con ms-users: {}", username, e.getMessage(), e);
            return false;
        }
    }
}