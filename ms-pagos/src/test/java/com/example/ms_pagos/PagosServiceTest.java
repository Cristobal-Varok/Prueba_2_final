package com.example.ms_pagos;

import com.example.ms_pagos.client.CartClient;
import com.example.ms_pagos.client.OrderClient;
import com.example.ms_pagos.client.UserClient;
import com.example.ms_pagos.dto.OrderDTO;
import com.example.ms_pagos.dto.request.PagosRequestDTO;
import com.example.ms_pagos.dto.response.PagosResponseDTO;
import com.example.ms_pagos.exception.custom.OrderAlreadyPaidException;
import com.example.ms_pagos.exception.custom.PaymentAmountMismatchException;
import com.example.ms_pagos.exception.custom.PaymentNotFoundException;
import com.example.ms_pagos.exception.custom.UserNotFoundException;
import com.example.ms_pagos.model.Pagos;
import com.example.ms_pagos.model.PagosMethod;
import com.example.ms_pagos.model.PagosStatus;
import com.example.ms_pagos.repository.PagosRepository;
import com.example.ms_pagos.service.PagosService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagosServiceTest {

    @Mock
    private PagosRepository pagosRepository;

    @Mock
    private OrderClient orderClient;

    @Mock
    private CartClient cartClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private PagosService pagosService;

    //Helper

    private Pagos buildPago() {
        return Pagos.builder()
                .paymentId(1L)
                .orderId(10L)
                .userId(1L)
                .amount(99.99)
                .method(PagosMethod.CREDIT_CARD)
                .status(PagosStatus.COMPLETED)
                .transactionId("TXN-123")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    private OrderDTO buildOrder() {
        OrderDTO order = new OrderDTO();
        order.setOrderId(10L);
        order.setTotalAmount(99.99);
        return order;
    }

    // getPaymentById

    @Test
    void deberiaRetornarPagoPorId() {
        when(pagosRepository.findById(1L)).thenReturn(Optional.of(buildPago()));

        PagosResponseDTO resultado = pagosService.getPaymentById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getPaymentId());
        assertEquals(PagosStatus.COMPLETED, resultado.getStatus());
        verify(pagosRepository).findById(1L);
    }

    @Test
    void deberiaLanzarExcepcionCuandoPagoNoExiste() {
        when(pagosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () ->
                pagosService.getPaymentById(99L));
    }

    // getPaymentsByOrder

    @Test
    void deberiaRetornarPagosPorOrden() {
        when(pagosRepository.findByOrderId(10L)).thenReturn(List.of(buildPago()));

        List<PagosResponseDTO> resultado = pagosService.getPaymentsByOrder(10L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getOrderId());
    }

    //getPaymentsByUser

    @Test
    void deberiaRetornarPagosPorUsuario() {
        when(pagosRepository.findByUserId(1L)).thenReturn(List.of(buildPago()));

        List<PagosResponseDTO> resultado = pagosService.getPaymentsByUser(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getUserId());
    }

    //processPayment

    @Test
    void deberiaProcesarPagoCorrectamente() {
        PagosRequestDTO request = new PagosRequestDTO();
        request.setOrderId(10L);
        request.setAmount(99.99);
        request.setMethod(PagosMethod.CREDIT_CARD);

        when(userClient.userExists("testuser")).thenReturn(true);
        when(orderClient.getOrderById(10L)).thenReturn(buildOrder());
        when(pagosRepository.existsByOrderId(10L)).thenReturn(false);
        when(pagosRepository.save(any(Pagos.class))).thenReturn(buildPago());

        PagosResponseDTO resultado = pagosService.processPayment("testuser", request);

        assertNotNull(resultado);
        assertEquals(PagosStatus.COMPLETED, resultado.getStatus());
        verify(pagosRepository).save(any(Pagos.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoUsuarioNoExiste() {
        PagosRequestDTO request = new PagosRequestDTO();
        request.setOrderId(10L);
        request.setAmount(99.99);
        request.setMethod(PagosMethod.CREDIT_CARD);

        when(userClient.userExists("noexiste")).thenReturn(false);

        assertThrows(UserNotFoundException.class, () ->
                pagosService.processPayment("noexiste", request));
    }

    @Test
    void deberiaLanzarExcepcionCuandoOrdenYaPagada() {
        PagosRequestDTO request = new PagosRequestDTO();
        request.setOrderId(10L);
        request.setAmount(99.99);
        request.setMethod(PagosMethod.CREDIT_CARD);

        when(userClient.userExists("testuser")).thenReturn(true);
        when(orderClient.getOrderById(10L)).thenReturn(buildOrder());
        when(pagosRepository.existsByOrderId(10L)).thenReturn(true);

        assertThrows(OrderAlreadyPaidException.class, () ->
                pagosService.processPayment("testuser", request));
    }

    @Test
    void deberiaLanzarExcepcionCuandoMontoNoCoinicde() {
        PagosRequestDTO request = new PagosRequestDTO();
        request.setOrderId(10L);
        request.setAmount(50.0);
        request.setMethod(PagosMethod.CREDIT_CARD);

        when(userClient.userExists("testuser")).thenReturn(true);
        when(orderClient.getOrderById(10L)).thenReturn(buildOrder());
        when(pagosRepository.existsByOrderId(10L)).thenReturn(false);

        assertThrows(PaymentAmountMismatchException.class, () ->
                pagosService.processPayment("testuser", request));
    }
}