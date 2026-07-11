package com.example.ms_order;

import com.example.ms_order.controller.OrderController;
import com.example.ms_order.model.Order;
import com.example.ms_order.model.enums.OrderStatus;
import com.example.ms_order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllOrders() {
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());
        ResponseEntity<CollectionModel<com.example.ms_order.dto.OrderDto>> response = orderController.getAllOrders();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetOrderById() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        when(orderService.getOrderById(1L)).thenReturn(order);
        ResponseEntity<com.example.ms_order.dto.OrderDto> response = orderController.getOrderById(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteOrder() {
        when(orderService.deleteOrder(1L)).thenReturn("Order successfully deleted");
        ResponseEntity<Void> response = orderController.deleteOrder(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}