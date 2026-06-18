package com.example.ms_tickets;

import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.model.TicketStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void testModel_GettersAndSetters_Success() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setUsername("benja");
        ticket.setStatus(TicketStatus.ABIERTO);

        assertEquals(1L, ticket.getId());
        assertEquals("benja", ticket.getUsername());
        assertEquals(TicketStatus.ABIERTO, ticket.getStatus());
    }

    @Test
    void testModel_ToString_NotEmpty() {
        Ticket ticket = new Ticket();
        ticket.setId(45L);
        ticket.setUsername("admin");

        String result = ticket.toString();
        assertNotNull(result);
    }
}