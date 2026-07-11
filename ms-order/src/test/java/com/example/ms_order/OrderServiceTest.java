package com.example.ms_order;

import com.example.ms_order.client.ProductClient;
import com.example.ms_order.exception.custom.OrderNotFoundException;
import com.example.ms_order.model.Order;
import com.example.ms_order.model.enums.OrderStatus;
import com.example.ms_order.repository.OrderRepository;
import com.example.ms_order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository repo;
    @Mock private ProductClient client;
    @InjectMocks private OrderService service;

    @Test
    void testGetOrderByIdSuccess() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Order()));
        assertNotNull(service.getOrderById(1L));
    }

    @Test
    void testGetOrderByIdNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> service.getOrderById(1L));
    }

    @Test
    void testDeleteOrder() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Order()));
        assertEquals("Order successfully deleted", service.deleteOrder(1L));
        verify(repo, times(1)).delete(any());
    }

    @Test
    void testUpdatePaymentStatusValidTransition() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING); // PENDING -> PAID es válida
        when(repo.findById(1L)).thenReturn(Optional.of(order));
        when(repo.save(any())).thenReturn(order);

        Order updated = service.updatePaymentStatus(1L, "PAID");
        assertEquals("PAID", updated.getPaymentStatus());
        assertEquals(OrderStatus.PAID, updated.getStatus());
    }

    @Test
    void testUpdatePaymentStatusInvalidTransition() {
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED); // DELIVERED es terminal
        when(repo.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                service.updatePaymentStatus(1L, "PAID"));
    }

    @Test
    void testChangeOrderStatusInvalidTransition() {
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);
        when(repo.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                service.changeOrderStatus(1L, OrderStatus.PENDING));
    }
}