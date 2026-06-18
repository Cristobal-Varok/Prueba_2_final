package com.example.ms_tickets;

import com.example.ms_tickets.dto.TicketDTO;
import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.model.TicketStatus;
import com.example.ms_tickets.repository.TicketRepository;
import com.example.ms_tickets.service.TicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;

    @InjectMocks private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testService_CreateTicket_Success() {
        TicketDTO dto = new TicketDTO();
        dto.setDescription("Problema con mi compra");

        Ticket mockSaved = new Ticket();
        mockSaved.setId(100L);
        mockSaved.setUsername("javier");
        mockSaved.setStatus(TicketStatus.ABIERTO);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(mockSaved);

        Ticket result = ticketService.createTicket("javier", dto);

        assertNotNull(result);
        assertEquals("javier", result.getUsername());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void testService_GetMyTickets_WithItems() {
        Ticket mockTicket = new Ticket();
        mockTicket.setId(1L);
        mockTicket.setUsername("javier");

        when(ticketRepository.findByUsername("javier")).thenReturn(Arrays.asList(mockTicket));

        List<Ticket> result = ticketService.getMyTickets("javier");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("javier", result.get(0).getUsername());
    }

    @Test
    void testService_GetMyTickets_Empty() {
        when(ticketRepository.findByUsername("maria")).thenReturn(Collections.emptyList());

        List<Ticket> result = ticketService.getMyTickets("maria");

        assertTrue(result.isEmpty());
    }
}