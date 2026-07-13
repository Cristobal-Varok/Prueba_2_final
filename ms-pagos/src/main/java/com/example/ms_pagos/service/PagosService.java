package com.example.ms_pagos.service;

import com.example.ms_pagos.client.CartClient;
import com.example.ms_pagos.client.OrderClient;
import com.example.ms_pagos.client.UserClient;
import com.example.ms_pagos.dto.OrderDTO;
import com.example.ms_pagos.dto.request.PagosRequestDTO;
import com.example.ms_pagos.dto.response.PagosResponseDTO;
import com.example.ms_pagos.exception.custom.*;
import com.example.ms_pagos.model.Pagos;
import com.example.ms_pagos.model.PagosStatus;
import com.example.ms_pagos.repository.PagosRepository;
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
public class PagosService {

    private final PagosRepository pagosRepository;
    private final OrderClient orderClient;
    private final CartClient cartClient;
    private final UserClient userClient;

    public Pagos findByIdOrThrow(Long paymentId) {
        log.debug("Buscando pago por ID: {}", paymentId);
        return pagosRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("Pago no encontrado - ID: {}", paymentId);
                    return new PaymentNotFoundException("Pago no encontrado con ID: " + paymentId);
                });
    }

    public List<PagosResponseDTO> getPaymentsByUser(Long userId) {
        log.debug("Obteniendo pagos del usuario: {}", userId);
        return pagosRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PagosResponseDTO> getPaymentsByOrder(Long orderId) {
        log.debug("Obteniendo pagos por orden: {}", orderId);
        return pagosRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public boolean paymentExistsByOrder(Long orderId) {
        log.debug("Verificando si existe pago para orden: {}", orderId);
        return pagosRepository.existsByOrderId(orderId);
    }

    @Transactional
    public PagosResponseDTO processPayment(String username, PagosRequestDTO request) {
        log.info("Procesando pago - Usuario: {}, OrderId: {}, Monto: ${}",
                username, request.getOrderId(), request.getAmount());

        // 1. Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // 2. Obtener la orden de compra
        OrderDTO order;
        try {
            order = orderClient.getOrderById(request.getOrderId());
        } catch (Exception e) {
            log.error("Error al obtener la orden: {}", e.getMessage());
            throw new OrderNotFoundException("Orden no encontrada con ID: " + request.getOrderId());
        }

        // 3. Validar que la orden no esté ya pagada
        if (paymentExistsByOrder(request.getOrderId())) {
            log.warn("Ya existe un pago para la orden: {}", request.getOrderId());
            throw new OrderAlreadyPaidException("Ya existe un pago procesado para esta orden");
        }

        // 4. Validar que el monto coincida con el total de la orden
        if (!order.getTotalAmount().equals(request.getAmount())) {
            log.warn("Monto no coincide - Orden total: ${}, Pago: ${}", order.getTotalAmount(), request.getAmount());
            throw new PaymentAmountMismatchException("El monto del pago no coincide con el total de la orden. " +
                    "Total de la orden: $" + order.getTotalAmount());
        }

        // 5. Simular pasarela de pago
        String transactionId = UUID.randomUUID().toString();
        PagosStatus status;
        String errorMessage = null;

        try {
            status = PagosStatus.COMPLETED;
            log.debug("Pago simulado exitoso, transactionId: {}", transactionId);
        } catch (Exception e) {
            status = PagosStatus.FAILED;
            errorMessage = "Error en la pasarela de pago: " + e.getMessage();
            log.error(errorMessage);
            throw new PaymentProcessingException(errorMessage);
        }

        // 6. Guardar el pago
        Pagos pagos = Pagos.builder()
                .orderId(request.getOrderId())
                .userId(order.getUserId()) // ← corregido
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(status)
                .transactionId(transactionId)
                .completedAt(status == PagosStatus.COMPLETED ? LocalDateTime.now() : null)
                .errorMessage(errorMessage)
                .build();

        Pagos saved = pagosRepository.save(pagos);
        log.info("Pago guardado con id: {}, estado: {}", saved.getPaymentId(), saved.getStatus());

        // 7. Si el pago fue exitoso, actualizar la orden y limpiar el carrito
        if (status == PagosStatus.COMPLETED) {
            try {
                orderClient.updateOrderPaymentStatus(request.getOrderId(), "PAID");
                log.info("Estado de orden actualizado a PAID para orden: {}", request.getOrderId());
            } catch (Exception e) {
                log.error("Error al actualizar estado de la orden: {}", e.getMessage());
            }

            try {
                cartClient.clearCart(); // ← corregido
                log.info("Carrito limpiado para usuario: {}", username);
            } catch (Exception e) {
                log.error("Error al limpiar el carrito: {}", e.getMessage());
            }
        }

        return mapToResponseDTO(saved);
    }

    public PagosResponseDTO getPaymentById(Long paymentId) {
        log.debug("Obteniendo pago por id: {}", paymentId);
        Pagos pagos = findByIdOrThrow(paymentId);
        return mapToResponseDTO(pagos);
    }

    private PagosResponseDTO mapToResponseDTO(Pagos pagos) {
        return PagosResponseDTO.builder()
                .paymentId(pagos.getPaymentId())
                .orderId(pagos.getOrderId())
                .userId(pagos.getUserId())
                .amount(pagos.getAmount())
                .method(pagos.getMethod())
                .status(pagos.getStatus())
                .transactionId(pagos.getTransactionId())
                .createdAt(pagos.getCreatedAt())
                .completedAt(pagos.getCompletedAt())
                .errorMessage(pagos.getErrorMessage())
                .build();
    }
}