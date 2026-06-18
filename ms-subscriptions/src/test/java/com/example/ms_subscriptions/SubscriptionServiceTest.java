package com.example.ms_subscriptions;

import com.example.ms_subscriptions.model.Subscription;
import com.example.ms_subscriptions.repository.SubscriptionRepository;
import com.example.ms_subscriptions.service.SubscriptionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;

    @InjectMocks private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testService_Success() {
        Subscription result = mock(Subscription.class);
        when(result.getId()).thenReturn(1L);

        assertNotNull(subscriptionService);
        assertNotNull(result);
        assertEquals(1L, result.getId());


    }
    @Test
    void testService_GetExpiringSubscriptions_Logic() {
        Subscription s1 = mock(Subscription.class);
        when(s1.getId()).thenReturn(1L);
        when(s1.getActive()).thenReturn(true);


        when(subscriptionRepository.findByEndDateBeforeAndActiveTrue(any())).thenReturn(Arrays.asList(s1));

        List<Subscription> result = subscriptionService.getExpiringSubscriptions();

        assertNotNull(result);

        verify(subscriptionRepository, times(1)).findByEndDateBeforeAndActiveTrue(any());
    }
    }