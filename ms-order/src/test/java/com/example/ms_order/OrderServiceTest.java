package com.example.ms_order;

import com.example.ms_order.client.ProductClient;
import com.example.ms_order.exception.custom.OrderNotFoundException;
import com.example.ms_order.model.Order;
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
    }

    @Test
    void testUpdatePaymentStatus() {
        Order o = new Order();
        when(repo.findById(1L)).thenReturn(Optional.of(o));
        when(repo.save(any())).thenReturn(o);
        service.updatePaymentStatus(1L, "PAID");
        assertEquals("PAID", o.getPaymentStatus());
    }
}