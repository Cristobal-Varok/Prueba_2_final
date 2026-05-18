package com.example.ms_tickets.repository;

import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUsername(String username);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByUsernameAndStatus(String username, TicketStatus status);
}