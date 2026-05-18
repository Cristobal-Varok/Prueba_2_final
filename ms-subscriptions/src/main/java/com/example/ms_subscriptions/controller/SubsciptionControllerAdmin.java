package com.example.ms_subscriptions.controller;

import com.example.ms_subscriptions.dto.SubscriptionResponseDTO;
import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subscriptions/admin")
@RequiredArgsConstructor
public class SubsciptionControllerAdmin {

    private static final Logger log = LoggerFactory.getLogger(SubsciptionControllerAdmin.class);

    private final SubscriptionService subscriptionService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllSubscriptions() {
        log.info("ADMIN - Solicitando todas las suscripciones activas");

        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getAllActiveSubscriptions().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        log.debug("ADMIN - Total de suscripciones activas encontradas: {}", subscriptions.size());

        return ResponseEntity.ok(Map.of(
                "subscriptions", subscriptions,
                "total", subscriptions.size()
        ));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSubscriptionsByType(@PathVariable String type) {
        log.info("ADMIN - Solicitando suscripciones por tipo: {}", type);

        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getSubscriptionsByType(type).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        log.debug("ADMIN - Suscripciones tipo {} encontradas: {}", type, subscriptions.size());

        return ResponseEntity.ok(Map.of(
                "type", type.toUpperCase(),
                "subscriptions", subscriptions,
                "total", subscriptions.size()
        ));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getExpiringSubscriptions() {
        log.info("ADMIN - Solicitando suscripciones próximas a vencer (30 días)");

        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getExpiringSubscriptions().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        log.debug("ADMIN - Suscripciones próximas a vencer encontradas: {}", subscriptions.size());

        return ResponseEntity.ok(Map.of(
                "subscriptions", subscriptions,
                "total", subscriptions.size()
        ));
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserSubscription(@PathVariable String username) {
        log.info("ADMIN - Solicitando suscripción del usuario: {}", username);

        return subscriptionService.getSubscriptionByUsername(username)
                .map(subscription -> {
                    log.debug("ADMIN - Suscripción encontrada para: {}", username);
                    return ResponseEntity.ok(Map.of(
                            "hasSubscription", subscription.getActive(),
                            "subscription", convertToResponseDTO(subscription)
                    ));
                })
                .orElseGet(() -> {
                    log.debug("ADMIN - Usuario sin suscripción: {}", username);
                    return ResponseEntity.ok(Map.of(
                            "hasSubscription", false,
                            "message", "El usuario no tiene suscripción"
                    ));
                });
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