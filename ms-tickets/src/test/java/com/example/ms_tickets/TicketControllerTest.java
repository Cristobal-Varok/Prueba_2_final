package com.example.ms_tickets;

import com.example.ms_tickets.controller.TicketController;
import com.example.ms_tickets.controller.TicketControllerAdmin;
import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.model.TicketStatus;
import com.example.ms_tickets.service.TicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class TicketControllerTest {

    @Mock private TicketService ticketService;
    @Mock private Authentication authentication;

    private TicketController ticketController;
    private TicketControllerAdmin ticketControllerAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.ticketController = new TicketController(ticketService);
        this.ticketControllerAdmin = new TicketControllerAdmin(ticketService);
        when(authentication.getName()).thenReturn("javier");
    }

    @Test
    void testController_GetMyTickets_HttpOk_WithItems() throws Exception {
        Ticket t1 = new Ticket();
        t1.setId(1L);
        t1.setUsername("javier");

        when(ticketService.getMyTickets("javier")).thenReturn(Arrays.asList(t1));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(ticketController).build();
        mockMvc.perform(get("/")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON));

        assertNotNull(mockMvc);
    }

    @Test
    void testController_GetMyTickets_HttpOk_EmptyList() throws Exception {
        when(ticketService.getMyTickets("javier")).thenReturn(Collections.emptyList());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(ticketController).build();
        mockMvc.perform(get("/")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON));

        assertNotNull(mockMvc);
    }

    @Test
    void testControllerAdmin_GetAllTickets_HttpOk() throws Exception {
        Ticket t1 = new Ticket();
        t1.setId(2L);
        t1.setStatus(TicketStatus.ABIERTO);

        when(ticketService.getAllTickets()).thenReturn(Arrays.asList(t1));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(ticketControllerAdmin).build();
        mockMvc.perform(get("/")
                .contentType(MediaType.APPLICATION_JSON));

        assertNotNull(mockMvc);
    }
}