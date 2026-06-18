package com.example.ms_subscriptions;

import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.model.SubscriptionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionTest {

    @Test
    void testModel_GettersAndSetters_Success() {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUsername("javier");

        // Cambia "PREMIUM" por el valor real de tu enum si es diferente
        // subscription.setType(SubscriptionType.PREMIUM);

        assertEquals(1L, subscription.getId());
        assertEquals("javier", subscription.getUsername());
    }
}