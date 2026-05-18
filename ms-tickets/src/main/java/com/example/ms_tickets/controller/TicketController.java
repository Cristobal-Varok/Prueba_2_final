package com.example.ms_tickets.controller;

import com.example.ms_tickets.dto.TicketDTO;
import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;

    // ========== ENDPOINTS PARA USUARIOS ==========

    // Crear ticket
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createTicket(Authentication authentication, @Valid @RequestBody TicketDTO ticketDTO) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de crear ticket - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Creando ticket - Usuario: {}, Asunto: {}", username, ticketDTO.getSubject());

        Ticket ticket = ticketService.createTicket(username, ticketDTO);

        log.info("Ticket creado exitosamente - ID: {}, Usuario: {}", ticket.getId(), username);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Ticket creado correctamente",
                "ticket", Map.of(
                        "id", ticket.getId(),
                        "subject", ticket.getSubject(),
                        "description", ticket.getDescription(),
                        "status", ticket.getStatus(),
                        "createdAt", ticket.getCreatedAt()
                )
        ));
    }

    // Obtener mis tickets
    @GetMapping("/my-tickets")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMyTickets(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener mis tickets - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Obteniendo tickets del usuario: {}", username);

        List<Ticket> tickets = ticketService.getMyTickets(username);

        log.debug("Tickets encontrados para usuario {}: {}", username, tickets.size());

        return ResponseEntity.ok(Map.of(
                "tickets", tickets,
                "total", tickets.size()
        ));
    }

    // Obtener un ticket específico (solo si es mío)
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getTicketById(Authentication authentication, @PathVariable Long ticketId) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener ticket - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Obteniendo ticket ID: {} para usuario: {}", ticketId, username);

        Ticket ticket = ticketService.getMyTicketById(ticketId, username);

        log.debug("Ticket encontrado - ID: {}, Usuario: {}", ticketId, username);

        return ResponseEntity.ok(Map.of("ticket", ticket));
    }

    // Eliminar mi ticket
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteMyTicket(Authentication authentication, @PathVariable Long ticketId) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de eliminar ticket - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.warn("Eliminando ticket - ID: {}, Usuario: {}", ticketId, username);

        ticketService.deleteMyTicket(ticketId, username);

        log.info("Ticket eliminado exitosamente - ID: {}, Usuario: {}", ticketId, username);

        return ResponseEntity.ok(Map.of("message", "Ticket eliminado correctamente"));
    }
}