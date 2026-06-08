package com.example.ms_tickets.controller;

import com.example.ms_tickets.dto.TicketDTO;
import com.example.ms_tickets.model.Ticket;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tickets (Usuario)", description = "Endpoints para usuarios gestionar sus tickets")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);
    private final TicketService ticketService;

    @Operation(summary = "Crear ticket", description = "Crea un nuevo ticket de soporte")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createTicket(Authentication authentication, @Valid @RequestBody TicketDTO ticketDTO) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Creando ticket - Usuario: {}", username);

        Ticket ticket = ticketService.createTicket(username, ticketDTO);

        ticket.add(linkTo(methodOn(TicketController.class).getMyTickets(authentication)).withRel("myTickets"));
        ticket.add(linkTo(methodOn(TicketController.class).getTicketById(authentication, ticket.getId())).withSelfRel());
        ticket.add(linkTo(methodOn(TicketController.class).deleteMyTicket(authentication, ticket.getId())).withRel("delete"));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Ticket creado correctamente",
                "ticket", ticket
        ));
    }

    @Operation(summary = "Obtener mis tickets", description = "Devuelve todos los tickets del usuario autenticado")
    @GetMapping("/my-tickets")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMyTickets(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        List<Ticket> tickets = ticketService.getMyTickets(username);

        tickets.forEach(ticket -> {
            ticket.add(linkTo(methodOn(TicketController.class).getTicketById(authentication, ticket.getId())).withSelfRel());
            ticket.add(linkTo(methodOn(TicketController.class).deleteMyTicket(authentication, ticket.getId())).withRel("delete"));
        });

        return ResponseEntity.ok(Map.of(
                "tickets", tickets,
                "total", tickets.size(),
                "_links", Map.of("self", linkTo(methodOn(TicketController.class).getMyTickets(authentication)).withSelfRel())
        ));
    }

    @Operation(summary = "Obtener ticket por ID", description = "Devuelve un ticket específico")
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getTicketById(Authentication authentication, @PathVariable Long ticketId) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        Ticket ticket = ticketService.getMyTicketById(ticketId, username);

        ticket.add(linkTo(methodOn(TicketController.class).getTicketById(authentication, ticketId)).withSelfRel());
        ticket.add(linkTo(methodOn(TicketController.class).getMyTickets(authentication)).withRel("myTickets"));
        ticket.add(linkTo(methodOn(TicketController.class).deleteMyTicket(authentication, ticketId)).withRel("delete"));

        return ResponseEntity.ok(Map.of("ticket", ticket));
    }

    @Operation(summary = "Eliminar mi ticket", description = "Elimina un ticket del usuario autenticado")
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteMyTicket(Authentication authentication, @PathVariable Long ticketId) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        ticketService.deleteMyTicket(ticketId, username);

        return ResponseEntity.ok(Map.of(
                "message", "Ticket eliminado correctamente",
                "_links", Map.of("myTickets", linkTo(methodOn(TicketController.class).getMyTickets(authentication)).withRel("myTickets"))
        ));
    }
}