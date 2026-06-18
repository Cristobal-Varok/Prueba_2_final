package com.example.ms_subscriptions;

import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionRepositoryTest {

    @Mock private SubscriptionRepository subscriptionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRepository_FindById_ReturnsItem() {
        Subscription s1 = new Subscription();
        s1.setId(1L);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(s1));

        Optional<Subscription> result = subscriptionRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testRepository_FindById_ReturnsEmpty() {
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionRepository.findById(99L);

        assertFalse(result.isPresent());
    }
}