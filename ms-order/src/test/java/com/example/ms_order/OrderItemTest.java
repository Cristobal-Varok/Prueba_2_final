package com.example.ms_order;

import com.example.ms_order.model.OrderItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderItemTest {

    @Test
    void testOrderItemData() {
        OrderItem item = OrderItem.builder().productId(100L).quantity(2).build();
        assertEquals(100L, item.getProductId());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void testItemPrice() {
        OrderItem item = new OrderItem();
        item.setPrice(50.0);
        assertEquals(50.0, item.getPrice());
    }

    @Test
    void testItemQuantityUpdate() {
        OrderItem item = new OrderItem();
        item.setQuantity(5);
        assertEquals(5, item.getQuantity());
    }
}