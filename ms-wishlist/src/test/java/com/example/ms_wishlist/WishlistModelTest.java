package com.example.ms_wishlist;

import com.example.ms_wishlist.model.WishlistItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WishlistItemTest {

    @Test
    void testModel_GettersAndSetters_Success() {
        WishlistItem item = new WishlistItem();
        item.setId(10L);
        item.setUsername("diego");
        item.setProductId("prod-999");

        assertEquals(10L, item.getId());
        assertEquals("diego", item.getUsername());
        assertEquals("prod-999", item.getProductId());
    }

    @Test
    void testModel_EmptyConstructorAndSetters() {
        WishlistItem item = new WishlistItem();
        item.setId(5L);
        item.setUsername("andres");

        assertEquals(5L, item.getId());
        assertEquals("andres", item.getUsername());
    }

    @Test
    void testModel_ToString_ContainsData() {
        WishlistItem item = new WishlistItem();
        item.setId(1L);
        item.setUsername("javier");
        item.setProductId("prod-123");

        String toStringResult = item.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("javier"), "El toString debería incluir el username");
        assertTrue(toStringResult.contains("prod-123"), "El toString debería incluir el productId");
    }
}