package com.example.ms_subscriptions.service;

import com.example.ms_subscriptions.client.UserClient;
import com.example.ms_subscriptions.dto.SubscriptionDTO;
import com.example.ms_subscriptions.exception.custom.*;
import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.model.SubscriptionType;
import com.example.ms_subscriptions.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final UserClient userClient;

    // Buscar por username - lanza excepción si no existe
    public Subscription findByUsernameOrThrow(String username) {
        log.debug("Buscando suscripción por username: {}", username);
        return subscriptionRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Suscripción no encontrada para: {}", username);
                    return new SubscriptionNotFoundException("No tienes una suscripción activa: " + username);
                });
    }

    // Crear o renovar suscripción
    public Subscription subscribe(String username, SubscriptionDTO dto) {
        log.info("Procesando suscripción para usuario: {}", username);

        // Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe al intentar suscribirse: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // Validar tipo de suscripción
        SubscriptionType type;
        try {
            type = SubscriptionType.valueOf(dto.getType().toUpperCase());
            log.debug("Tipo de suscripción validado: {}", type);
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de suscripción inválido: {}", dto.getType());
            throw new InvalidSubscriptionTypeException("Tipo de suscripción inválido. Use: BASICA, PREMIUM, VIP");
        }

        // Validar duración
        if (dto.getDurationMonths() < 1 || dto.getDurationMonths() > 12) {
            log.warn("Duración inválida para usuario {}: {} meses", username, dto.getDurationMonths());
            throw new IllegalArgumentException("La duración debe ser entre 1 y 12 meses");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusMonths(dto.getDurationMonths());

        Optional<Subscription> existingOpt = subscriptionRepository.findByUsername(username);

        if (existingOpt.isPresent()) {
            Subscription existing = existingOpt.get();

            if (existing.getActive()) {
                // Renovar suscripción existente activa
                log.info("Renovando suscripción activa para usuario: {}", username);
                existing.setType(type);
                existing.setStartDate(now);
                existing.setEndDate(endDate);
                existing.setActive(true);
                existing.setCancelledAt(null);
                Subscription saved = subscriptionRepository.save(existing);
                log.info("Suscripción renovada exitosamente para: {}, nueva fecha fin: {}", username, endDate);
                return saved;
            } else {
                // Reactivar suscripción cancelada
                log.info("Reactivando suscripción cancelada para usuario: {}", username);
                existing.setType(type);
                existing.setStartDate(now);
                existing.setEndDate(endDate);
                existing.setActive(true);
                existing.setCancelledAt(null);
                Subscription saved = subscriptionRepository.save(existing);
                log.info("Suscripción reactivada exitosamente para: {}", username);
                return saved;
            }
        } else {
            // Crear nueva suscripción
            log.info("Creando nueva suscripción para usuario: {}", username);
            Subscription subscription = Subscription.builder()
                    .username(username)
                    .type(type)
                    .startDate(now)
                    .endDate(endDate)
                    .active(true)
                    .build();
            Subscription saved = subscriptionRepository.save(subscription);
            log.info("Nueva suscripción creada exitosamente para: {}, Tipo: {}, Vence: {}",
                    username, type, endDate);
            return saved;
        }
    }

    // Obtener suscripción de un usuario
    public Optional<Subscription> getSubscriptionByUsername(String username) {
        log.debug("Consultando suscripción para: {}", username);
        return subscriptionRepository.findByUsername(username);
    }

    // Obtener suscripción lanzando excepción si no existe
    public Subscription getSubscriptionOrThrow(String username) {
        return findByUsernameOrThrow(username);
    }

    // Cancelar suscripción
    public Subscription cancelSubscription(String username) {
        log.warn("Cancelando suscripción para usuario: {}", username);
        Subscription subscription = findByUsernameOrThrow(username);

        if (!subscription.getActive()) {
            log.warn("Intento de cancelar suscripción ya inactiva para: {}", username);
            throw new SubscriptionNotActiveException("Tu suscripción ya está cancelada");
        }

        subscription.setActive(false);
        subscription.setCancelledAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Suscripción cancelada exitosamente para: {}", username);
        return saved;
    }

    // Verificar si un usuario tiene suscripción activa
    public boolean hasActiveSubscription(String username) {
        boolean hasActive = subscriptionRepository.existsByUsernameAndActiveTrue(username);
        log.debug("Verificación de suscripción activa para {}: {}", username, hasActive);
        return hasActive;
    }

    // Obtener todas las suscripciones activas (para ADMIN)
    public List<Subscription> getAllActiveSubscriptions() {
        log.debug("Obteniendo todas las suscripciones activas");
        List<Subscription> subscriptions = subscriptionRepository.findByActiveTrue();
        log.debug("Total de suscripciones activas: {}", subscriptions.size());
        return subscriptions;
    }

    // Obtener suscripciones por tipo (para ADMIN)
    public List<Subscription> getSubscriptionsByType(String type) {
        log.debug("Obteniendo suscripciones por tipo: {}", type);
        try {
            SubscriptionType subscriptionType = SubscriptionType.valueOf(type.toUpperCase());
            List<Subscription> subscriptions = subscriptionRepository.findByType(subscriptionType);
            log.debug("Suscripciones tipo {} encontradas: {}", type, subscriptions.size());
            return subscriptions;
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de suscripción inválido solicitado por ADMIN: {}", type);
            throw new InvalidSubscriptionTypeException("Tipo de suscripción inválido. Use: BASICA, PREMIUM, VIP");
        }
    }

    // Obtener suscripciones próximas a vencer (para ADMIN)
    public List<Subscription> getExpiringSubscriptions() {
        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        log.info("Buscando suscripciones que vencen en los próximos 30 días (hasta: {})", thirtyDaysFromNow);

        List<Subscription> subscriptions = subscriptionRepository.findByEndDateBeforeAndActiveTrue(thirtyDaysFromNow);
        log.info("Suscripciones próximas a vencer encontradas: {}", subscriptions.size());
        return subscriptions;
    }
}