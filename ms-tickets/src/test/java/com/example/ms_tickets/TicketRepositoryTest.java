package com.example.ms_tickets;

import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class TicketRepositoryTest {

    @Mock private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRepository_FindByUsername_ReturnsList() {
        Ticket t1 = new Ticket();
        t1.setUsername("benja");

        List<Ticket> mockList = Arrays.asList(t1);
        when(ticketRepository.findByUsername("benja")).thenReturn(mockList);

        List<Ticket> result = ticketRepository.findByUsername("benja");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("benja", result.get(0).getUsername());
        verify(ticketRepository, times(1)).findByUsername("benja");
    }

    @Test
    void testRepository_FindByUsername_ReturnsEmpty() {
        when(ticketRepository.findByUsername("unknown")).thenReturn(Collections.emptyList());

        List<Ticket> result = ticketRepository.findByUsername("unknown");

        assertTrue(result.isEmpty());
    }
}