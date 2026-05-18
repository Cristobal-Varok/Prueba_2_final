package com.example.ms_tickets.controller;

import com.example.ms_tickets.dto.TicketResponseDTO;
import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.model.TicketStatus;
import com.example.ms_tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets/admin")
@RequiredArgsConstructor
public class TicketControllerAdmin {

    private static final Logger log = LoggerFactory.getLogger(TicketControllerAdmin.class);

    private final TicketService ticketService;

    // Obtener todos los tickets
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTickets() {
        log.info("ADMIN - Solicitando todos los tickets");

        List<Ticket> tickets = ticketService.getAllTickets();

        log.debug("ADMIN - Total de tickets encontrados: {}", tickets.size());

        return ResponseEntity.ok(Map.of(
                "tickets", tickets,
                "total", tickets.size()
        ));
    }

    // Obtener tickets por estado
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTicketsByStatus(@PathVariable String status) {
        log.info("ADMIN - Solicitando tickets por estado: {}", status);

        try {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            List<Ticket> tickets = ticketService.getTicketsByStatus(ticketStatus);

            log.debug("ADMIN - Tickets con estado {} encontrados: {}", status, tickets.size());

            return ResponseEntity.ok(Map.of(
                    "status", status.toUpperCase(),
                    "tickets", tickets,
                    "total", tickets.size()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("ADMIN - Estado inválido solicitado: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Estado inválido. Use: ABIERTO, EN_PROCESO, RESUELTO, CERRADO"));
        }
    }

    // Responder a un ticket
    @PutMapping("/{ticketId}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> respondToTicket(@PathVariable Long ticketId, @Valid @RequestBody TicketResponseDTO responseDTO) {
        log.info("ADMIN - Respondiendo a ticket ID: {}", ticketId);

        Ticket ticket = ticketService.respondToTicket(ticketId, responseDTO);

        log.info("ADMIN - Respuesta enviada al ticket ID: {}, Nuevo estado: {}", ticketId, ticket.getStatus());

        return ResponseEntity.ok(Map.of(
                "message", "Respuesta enviada correctamente",
                "ticket", Map.of(
                        "id", ticket.getId(),
                        "status", ticket.getStatus(),
                        "adminResponse", ticket.getAdminResponse(),
                        "updatedAt", ticket.getUpdatedAt()
                )
        ));
    }

    // Cambiar estado de un ticket
    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeTicketStatus(@PathVariable Long ticketId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("ADMIN - Cambiando estado del ticket ID: {} a: {}", ticketId, status);

        Ticket ticket = ticketService.changeTicketStatus(ticketId, status);

        log.info("ADMIN - Estado del ticket ID: {} actualizado a: {}", ticketId, ticket.getStatus());

        return ResponseEntity.ok(Map.of(
                "message", "Estado actualizado correctamente",
                "ticket", Map.of(
                        "id", ticket.getId(),
                        "status", ticket.getStatus(),
                        "updatedAt", ticket.getUpdatedAt()
                )
        ));
    }

    // Eliminar cualquier ticket (solo ADMIN)
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAnyTicket(@PathVariable Long ticketId) {
        log.warn("ADMIN - Eliminando ticket ID: {}", ticketId);

        ticketService.deleteAnyTicket(ticketId);

        log.info("ADMIN - Ticket ID: {} eliminado exitosamente", ticketId);

        return ResponseEntity.ok(Map.of("message", "Ticket eliminado correctamente por ADMIN"));
    }
}