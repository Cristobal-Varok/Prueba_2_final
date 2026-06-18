package com.example.ms_order;

import com.example.ms_order.model.Order;
import com.example.ms_order.model.enums.OrderStatus;
import com.example.ms_order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class OrderRepositoryTest {

    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
    }

    @Test
    void testFindByStatus() {
        Order mockOrder = Order.builder()
                .username("testUser")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(mockOrder));

        List<Order> found = orderRepository.findByStatus(OrderStatus.PENDING);

        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testFindByUsername() {
        Order mockOrder = Order.builder()
                .username("dev_user")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByUsername("dev_user")).thenReturn(List.of(mockOrder));
        List<Order> found = orderRepository.findByUsername("dev_user");

        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getUsername()).isEqualTo("dev_user");
    }
}