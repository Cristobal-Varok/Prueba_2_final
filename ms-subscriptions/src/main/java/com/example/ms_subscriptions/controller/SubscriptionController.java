package com.example.ms_subscriptions.controller;

import com.example.ms_subscriptions.dto.SubscriptionDTO;
import com.example.ms_subscriptions.dto.SubscriptionResponseDTO;
import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Suscripciones (Usuario)", description = "Endpoints para que los usuarios gestionen sus suscripciones")
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);
    private final SubscriptionService subscriptionService;

    @Operation(summary = "Crear o renovar suscripción", description = "Crea una nueva suscripción o renueva una existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Suscripción creada/renovada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario no existe")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> subscribe(Authentication authentication, @Valid @RequestBody SubscriptionDTO dto) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Solicitud de suscripción - Usuario: {}, Tipo: {}, Duración: {} meses", username, dto.getType(), dto.getDurationMonths());

        Subscription subscription = subscriptionService.subscribe(username, dto);

        SubscriptionResponseDTO responseDTO = SubscriptionResponseDTO.builder()
                .id(subscription.getId())
                .username(subscription.getUsername())
                .type(subscription.getType().name())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .active(subscription.getActive())
                .build();

        responseDTO.add(linkTo(methodOn(SubscriptionController.class).getMySubscription(authentication)).withRel("mySubscription"));
        responseDTO.add(linkTo(methodOn(SubscriptionController.class).checkSubscription(authentication)).withRel("check"));
        responseDTO.add(linkTo(methodOn(SubscriptionController.class).cancelSubscription(authentication)).withRel("cancel"));

        log.info("Suscripción creada exitosamente - Usuario: {}, Tipo: {}", username, subscription.getType());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Suscripción creada correctamente",
                "subscription", responseDTO
        ));
    }

    @Operation(summary = "Obtener mi suscripción", description = "Devuelve los datos de la suscripción del usuario autenticado")
    @GetMapping("/my-subscription")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMySubscription(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Obteniendo suscripción - Usuario: {}", username);

        Optional<Subscription> subscriptionOpt = subscriptionService.getSubscriptionByUsername(username);

        if (subscriptionOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "hasSubscription", false,
                    "message", "No tienes una suscripción"
            ));
        }

        Subscription subscription = subscriptionOpt.get();
        SubscriptionResponseDTO responseDTO = SubscriptionResponseDTO.builder()
                .id(subscription.getId())
                .username(subscription.getUsername())
                .type(subscription.getType().name())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .active(subscription.getActive())
                .build();

        responseDTO.add(linkTo(methodOn(SubscriptionController.class).getMySubscription(authentication)).withSelfRel());
        responseDTO.add(linkTo(methodOn(SubscriptionController.class).subscribe(authentication, null)).withRel("renew"));
        responseDTO.add(linkTo(methodOn(SubscriptionController.class).cancelSubscription(authentication)).withRel("cancel"));
        responseDTO.add(linkTo(methodOn(SubscriptionController.class).checkSubscription(authentication)).withRel("check"));

        return ResponseEntity.ok(Map.of(
                "hasSubscription", subscription.getActive(),
                "subscription", responseDTO
        ));
    }

    @Operation(summary = "Verificar suscripción activa", description = "Comprueba si el usuario tiene una suscripción activa")
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> checkSubscription(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        boolean hasActive = subscriptionService.hasActiveSubscription(username);

        return ResponseEntity.ok(Map.of(
                "hasActiveSubscription", hasActive,
                "username", username,
                "_links", Map.of(
                        "subscribe", linkTo(methodOn(SubscriptionController.class).subscribe(authentication, null)).withRel("subscribe"),
                        "mySubscription", linkTo(methodOn(SubscriptionController.class).getMySubscription(authentication)).withRel("mySubscription")
                )
        ));
    }

    @Operation(summary = "Cancelar suscripción", description = "Cancela la suscripción activa del usuario")
    @DeleteMapping("/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> cancelSubscription(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.warn("Solicitud de cancelación de suscripción - Usuario: {}", username);

        Subscription subscription = subscriptionService.cancelSubscription(username);

        SubscriptionResponseDTO responseDTO = SubscriptionResponseDTO.builder()
                .id(subscription.getId())
                .username(subscription.getUsername())
                .type(subscription.getType().name())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .active(subscription.getActive())
                .build();

        responseDTO.add(linkTo(methodOn(SubscriptionController.class).subscribe(authentication, null)).withRel("renew"));
        responseDTO.add(linkTo(methodOn(SubscriptionController.class).checkSubscription(authentication)).withRel("check"));

        log.info("Suscripción cancelada exitosamente - Usuario: {}", username);

        return ResponseEntity.ok(Map.of(
                "message", "Suscripción cancelada correctamente",
                "subscription", responseDTO
        ));
    }
}