package com.example.ms_envios.service;

import com.example.ms_envios.client.OrderClient;
import com.example.ms_envios.client.UserClient;
import com.example.ms_envios.dto.OrderDTO;
import com.example.ms_envios.dto.CreateEnvioRequest;
import com.example.ms_envios.dto.response.EnvioResponseDTO;
import com.example.ms_envios.exception.custom.*;
import com.example.ms_envios.model.Envios;
import com.example.ms_envios.model.EnviosStatus;
import com.example.ms_envios.repository.EnviosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnviosService {

    private final EnviosRepository enviosRepository;
    private final OrderClient orderClient;
    private final UserClient userClient;

    public Envios findByIdOrThrow(Long shippingId) {
        log.debug("Buscando envío por ID: {}", shippingId);
        return enviosRepository.findById(shippingId)
                .orElseThrow(() -> {
                    log.warn("Envío no encontrado - ID: {}", shippingId);
                    return new ShippingNotFoundException("Envío no encontrado con ID: " + shippingId);
                });
    }

    public boolean shippingExistsByOrder(Long orderId) {
        log.debug("Verificando si existe envío para la orden: {}", orderId);
        return enviosRepository.existsByOrderId(orderId);
    }

    @Transactional
    public EnvioResponseDTO createShipping(String username, CreateEnvioRequest request) {
        log.info("Creando envío - Usuario: {}, Orden: {}", username, request.getOrderId());

        // 1. Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // 2. Obtener y validar la orden
        OrderDTO order;
        try {
            order = orderClient.getOrderById(request.getOrderId());
        } catch (Exception e) {
            log.error("Error al obtener la orden: {}", e.getMessage());
            throw new OrderNotFoundException("Orden no encontrada con ID: " + request.getOrderId());
        }

        // 3. Validar que la orden existe y está pagada
        if (order == null) {
            throw new OrderNotFoundException("Orden no encontrada con ID: " + request.getOrderId());
        }

        if (!"PAID".equals(order.getPaymentStatus())) {
            log.warn("La orden {} no está pagada. Estado actual: {}", request.getOrderId(), order.getPaymentStatus());
            throw new OrderNotPaidException("La orden debe estar pagada para poder generar el envío. Estado actual: " + order.getPaymentStatus());
        }

        // 4. Validar que no exista un envío previo para esta orden
        if (shippingExistsByOrder(request.getOrderId())) {
            log.warn("Ya existe un envío para la orden: {}", request.getOrderId());
            throw new ShippingAlreadyExistsException("Ya existe un envío para la orden: " + request.getOrderId());
        }

        // 5. Crear el envío
        Envios shipping = Envios.builder()
                .orderId(request.getOrderId())
                .userId(order.getClientId())
                .address(request.getAddress())
                .status(EnviosStatus.PENDING)
                .trackingNumber(generateTrackingNumber())
                .build();

        Envios saved = enviosRepository.save(shipping);
        log.info("Envío creado con id: {}, tracking: {}", saved.getShippingId(), saved.getTrackingNumber());

        return mapToResponseDTO(saved);
    }

    @Transactional
    public EnvioResponseDTO updateStatus(Long shippingId, EnviosStatus newStatus) {
        log.info("Actualizando estado del envío {} a {}", shippingId, newStatus);
        Envios shipping = findByIdOrThrow(shippingId);

        EnviosStatus oldStatus = shipping.getStatus();
        shipping.setStatus(newStatus);

        if (newStatus == EnviosStatus.SHIPPED && oldStatus != EnviosStatus.SHIPPED) {
            shipping.setShippedAt(LocalDateTime.now());
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
            log.debug("Fecha de envío y estimación actualizadas");
        } else if (newStatus == EnviosStatus.DELIVERED && oldStatus != EnviosStatus.DELIVERED) {
            shipping.setDeliveredAt(LocalDateTime.now());
            log.debug("Fecha de entrega registrada");
        }

        Envios updated = enviosRepository.save(shipping);
        log.info("Estado actualizado para envío {}", shippingId);
        return mapToResponseDTO(updated);
    }

    public EnvioResponseDTO getShipping(Long id) {
        log.debug("Consultando envío por id: {}", id);
        Envios shipping = findByIdOrThrow(id);
        return mapToResponseDTO(shipping);
    }

    public List<EnvioResponseDTO> getByOrder(Long orderId) {
        log.debug("Consultando envíos por orderId: {}", orderId);
        return enviosRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EnvioResponseDTO> getByUser(Long userId) {
        log.debug("Consultando envíos por userId: {}", userId);
        return enviosRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EnvioResponseDTO> getByStatus(EnviosStatus status) {
        log.debug("Consultando envíos por estado: {}", status);
        return enviosRepository.findByStatus(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private String generateTrackingNumber() {
        return "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private EnvioResponseDTO mapToResponseDTO(Envios shipping) {
        return EnvioResponseDTO.builder()
                .shippingId(shipping.getShippingId())
                .orderId(shipping.getOrderId())
                .userId(shipping.getUserId())
                .address(shipping.getAddress())
                .status(shipping.getStatus())
                .trackingNumber(shipping.getTrackingNumber())
                .createdAt(shipping.getCreatedAt())
                .shippedAt(shipping.getShippedAt())
                .estimatedDelivery(shipping.getEstimatedDelivery())
                .deliveredAt(shipping.getDeliveredAt())
                .build();
    }
}