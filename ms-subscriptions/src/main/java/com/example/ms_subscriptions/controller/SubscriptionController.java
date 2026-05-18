package com.example.ms_subscriptions.controller;

import com.example.ms_subscriptions.dto.SubscriptionDTO;
import com.example.ms_subscriptions.dto.SubscriptionResponseDTO;
import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;

    // Crear o renovar suscripción
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> subscribe(Authentication authentication, @Valid @RequestBody SubscriptionDTO dto) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de crear suscripción - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Solicitud de suscripción - Usuario: {}, Tipo: {}, Duración: {} meses",
                username, dto.getType(), dto.getDurationMonths());

        Subscription subscription = subscriptionService.subscribe(username, dto);

        log.info("Suscripción creada exitosamente - Usuario: {}, Tipo: {}, Activa: {}",
                username, subscription.getType(), subscription.getActive());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Suscripción creada correctamente",
                "subscription", convertToResponseDTO(subscription)
        ));
    }

    // Obtener mi suscripción
    @GetMapping("/my-subscription")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMySubscription(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener suscripción - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Obteniendo suscripción - Usuario: {}", username);

        Optional<Subscription> subscription = subscriptionService.getSubscriptionByUsername(username);

        if (subscription.isEmpty()) {
            log.debug("Usuario sin suscripción - Usuario: {}", username);
            return ResponseEntity.ok(Map.of(
                    "hasSubscription", false,
                    "message", "No tienes una suscripción"
            ));
        }

        log.debug("Suscripción encontrada - Usuario: {}, Activa: {}", username, subscription.get().getActive());
        return ResponseEntity.ok(Map.of(
                "hasSubscription", subscription.get().getActive(),
                "subscription", convertToResponseDTO(subscription.get())
        ));
    }

    // Verificar si tengo suscripción activa
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> checkSubscription(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de verificar suscripción - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        boolean hasActive = subscriptionService.hasActiveSubscription(username);

        log.debug("Verificación de suscripción - Usuario: {}, Activa: {}", username, hasActive);

        return ResponseEntity.ok(Map.of(
                "hasActiveSubscription", hasActive,
                "username", username
        ));
    }

    // Cancelar mi suscripción
    @DeleteMapping("/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> cancelSubscription(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de cancelar suscripción - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.warn("Solicitud de cancelación de suscripción - Usuario: {}", username);

        Subscription subscription = subscriptionService.cancelSubscription(username);

        log.info("Suscripción cancelada exitosamente - Usuario: {}, Tipo: {}", username, subscription.getType());

        return ResponseEntity.ok(Map.of(
                "message", "Suscripción cancelada correctamente",
                "subscription", convertToResponseDTO(subscription)
        ));
    }

    private SubscriptionResponseDTO convertToResponseDTO(Subscription subscription) {
        return new SubscriptionResponseDTO(
                subscription.getId(),
                subscription.getUsername(),
                subscription.getType().name(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getActive()
        );
    }
}