package com.example.ms_subscriptions.controller;

import com.example.ms_subscriptions.dto.SubscriptionResponseDTO;
import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.model.SubscriptionType;
import com.example.ms_subscriptions.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/subscriptions/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Suscripciones (Admin)", description = "Endpoints exclusivos para administradores")
public class SubsciptionControllerAdmin {

    private static final Logger log = LoggerFactory.getLogger(SubsciptionControllerAdmin.class);
    private final SubscriptionService subscriptionService;

    @Operation(summary = "Obtener todas las suscripciones activas", description = "Devuelve todas las suscripciones activas del sistema")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllSubscriptions() {
        log.info("ADMIN - Solicitando todas las suscripciones activas");

        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getAllActiveSubscriptions().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        subscriptions.forEach(sub -> {
            sub.add(linkTo(methodOn(SubsciptionControllerAdmin.class).getUserSubscription(sub.getUsername())).withRel("userSubscription"));
        });

        return ResponseEntity.ok(Map.of(
                "subscriptions", subscriptions,
                "total", subscriptions.size(),
                "_links", Map.of("self", linkTo(methodOn(SubsciptionControllerAdmin.class).getAllSubscriptions()).withSelfRel())
        ));
    }

    @Operation(summary = "Obtener suscripciones por tipo", description = "Filtra suscripciones por tipo (BASICA, PREMIUM, VIP)")
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSubscriptionsByType(@PathVariable String type) {
        log.info("ADMIN - Solicitando suscripciones por tipo: {}", type);

        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getSubscriptionsByType(type).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        subscriptions.forEach(sub -> {
            sub.add(linkTo(methodOn(SubsciptionControllerAdmin.class).getUserSubscription(sub.getUsername())).withRel("userSubscription"));
        });

        return ResponseEntity.ok(Map.of(
                "type", type.toUpperCase(),
                "subscriptions", subscriptions,
                "total", subscriptions.size(),
                "_links", Map.of("all", linkTo(methodOn(SubsciptionControllerAdmin.class).getAllSubscriptions()).withRel("all"))
        ));
    }

    @Operation(summary = "Obtener suscripciones próximas a vencer", description = "Devuelve suscripciones que vencen en los próximos 30 días")
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getExpiringSubscriptions() {
        log.info("ADMIN - Solicitando suscripciones próximas a vencer (30 días)");

        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getExpiringSubscriptions().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        subscriptions.forEach(sub -> {
            sub.add(linkTo(methodOn(SubsciptionControllerAdmin.class).getUserSubscription(sub.getUsername())).withRel("userSubscription"));
        });

        return ResponseEntity.ok(Map.of(
                "subscriptions", subscriptions,
                "total", subscriptions.size(),
                "_links", Map.of("all", linkTo(methodOn(SubsciptionControllerAdmin.class).getAllSubscriptions()).withRel("all"))
        ));
    }

    @Operation(summary = "Obtener suscripción de un usuario", description = "Devuelve la suscripción de un usuario específico (requiere ADMIN)")
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserSubscription(@PathVariable String username) {
        log.info("ADMIN - Solicitando suscripción del usuario: {}", username);

        return subscriptionService.getSubscriptionByUsername(username)
                .map(subscription -> {
                    SubscriptionResponseDTO responseDTO = convertToResponseDTO(subscription);
                    responseDTO.add(linkTo(methodOn(SubsciptionControllerAdmin.class).getUserSubscription(username)).withSelfRel());
                    responseDTO.add(linkTo(methodOn(SubsciptionControllerAdmin.class).getAllSubscriptions()).withRel("all"));

                    return ResponseEntity.ok(Map.of(
                            "hasSubscription", subscription.getActive(),
                            "subscription", responseDTO
                    ));
                })
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "hasSubscription", false,
                        "message", "El usuario no tiene suscripción"
                )));
    }

    private SubscriptionResponseDTO convertToResponseDTO(Subscription subscription) {
        return SubscriptionResponseDTO.builder()
                .id(subscription.getId())
                .username(subscription.getUsername())
                .type(subscription.getType().name())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .active(subscription.getActive())
                .build();
    }
}