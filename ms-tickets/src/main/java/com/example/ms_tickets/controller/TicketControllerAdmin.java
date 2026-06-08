package com.example.ms_tickets.controller;

import com.example.ms_tickets.dto.TicketResponseDTO;
import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.model.TicketStatus;
import com.example.ms_tickets.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/tickets/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tickets (Admin)", description = "Endpoints exclusivos para administradores")
public class TicketControllerAdmin {

    private static final Logger log = LoggerFactory.getLogger(TicketControllerAdmin.class);
    private final TicketService ticketService;

    @Operation(summary = "Obtener todos los tickets", description = "Devuelve todos los tickets del sistema")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTickets() {
        log.info("ADMIN - Solicitando todos los tickets");
        List<Ticket> tickets = ticketService.getAllTickets();

        tickets.forEach(ticket -> {
            ticket.add(linkTo(methodOn(TicketControllerAdmin.class).respondToTicket(ticket.getId(), null)).withRel("respond"));
            ticket.add(linkTo(methodOn(TicketControllerAdmin.class).changeTicketStatus(ticket.getId(), null)).withRel("changeStatus"));
            ticket.add(linkTo(methodOn(TicketControllerAdmin.class).deleteAnyTicket(ticket.getId())).withRel("delete"));
        });

        return ResponseEntity.ok(Map.of(
                "tickets", tickets,
                "total", tickets.size(),
                "_links", Map.of("self", linkTo(methodOn(TicketControllerAdmin.class).getAllTickets()).withSelfRel())
        ));
    }

    @Operation(summary = "Obtener tickets por estado", description = "Filtra tickets por estado")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTicketsByStatus(@PathVariable String status) {
        log.info("ADMIN - Tickets por estado: {}", status);
        try {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            List<Ticket> tickets = ticketService.getTicketsByStatus(ticketStatus);

            tickets.forEach(ticket -> {
                ticket.add(linkTo(methodOn(TicketControllerAdmin.class).respondToTicket(ticket.getId(), null)).withRel("respond"));
                ticket.add(linkTo(methodOn(TicketControllerAdmin.class).changeTicketStatus(ticket.getId(), null)).withRel("changeStatus"));
            });

            return ResponseEntity.ok(Map.of(
                    "status", status.toUpperCase(),
                    "tickets", tickets,
                    "total", tickets.size()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Estado inválido. Use: ABIERTO, EN_PROCESO, RESUELTO, CERRADO"));
        }
    }

    @Operation(summary = "Responder a un ticket", description = "Agrega respuesta del administrador")
    @PutMapping("/{ticketId}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> respondToTicket(@PathVariable Long ticketId, @Valid @RequestBody TicketResponseDTO responseDTO) {
        log.info("ADMIN - Respondiendo ticket ID: {}", ticketId);
        Ticket ticket = ticketService.respondToTicket(ticketId, responseDTO);

        ticket.add(linkTo(methodOn(TicketControllerAdmin.class).respondToTicket(ticketId, null)).withSelfRel());
        ticket.add(linkTo(methodOn(TicketControllerAdmin.class).getAllTickets()).withRel("allTickets"));
        ticket.add(linkTo(methodOn(TicketController.class).getTicketById(null, ticketId)).withRel("userView"));

        return ResponseEntity.ok(Map.of(
                "message", "Respuesta enviada correctamente",
                "ticket", ticket
        ));
    }

    @Operation(summary = "Cambiar estado de un ticket", description = "Modifica el estado de un ticket")
    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeTicketStatus(@PathVariable Long ticketId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("ADMIN - Cambiando estado ticket ID: {} a: {}", ticketId, status);
        Ticket ticket = ticketService.changeTicketStatus(ticketId, status);

        ticket.add(linkTo(methodOn(TicketControllerAdmin.class).changeTicketStatus(ticketId, null)).withSelfRel());
        ticket.add(linkTo(methodOn(TicketControllerAdmin.class).respondToTicket(ticketId, null)).withRel("respond"));
        ticket.add(linkTo(methodOn(TicketControllerAdmin.class).getAllTickets()).withRel("allTickets"));

        return ResponseEntity.ok(Map.of(
                "message", "Estado actualizado correctamente",
                "ticket", ticket
        ));
    }

    @Operation(summary = "Eliminar cualquier ticket", description = "Elimina un ticket del sistema")
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAnyTicket(@PathVariable Long ticketId) {
        log.warn("ADMIN - Eliminando ticket ID: {}", ticketId);
        ticketService.deleteAnyTicket(ticketId);

        return ResponseEntity.ok(Map.of(
                "message", "Ticket eliminado correctamente por ADMIN",
                "_links", Map.of("allTickets", linkTo(methodOn(TicketControllerAdmin.class).getAllTickets()).withRel("allTickets"))
        ));
    }
}