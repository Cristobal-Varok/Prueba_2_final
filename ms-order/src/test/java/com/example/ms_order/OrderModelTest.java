package com.example.ms_order;

import com.example.ms_order.model.Order;
import com.example.ms_order.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderModelTest {
    @Test
    void testOrderBuilderAndGetters() {
        Order order = Order.builder().id(1L).username("test").build();
        assertEquals(1L, order.getId());
        assertEquals("test", order.getUsername());
    }
    @Test
    void testOrderEquality() {
        Order o1 = Order.builder().id(1L).build();
        Order o2 = Order.builder().id(1L).build();
        assertEquals(o1, o2);
    }
    @Test
    void testOrderStatusAssignment() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}