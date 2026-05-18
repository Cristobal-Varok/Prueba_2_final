package com.example.ms_tickets.service;

import com.example.ms_tickets.dto.TicketDTO;
import com.example.ms_tickets.dto.TicketResponseDTO;
import com.example.ms_tickets.exception.custom.*;
import com.example.ms_tickets.model.Ticket;
import com.example.ms_tickets.model.TicketStatus;
import com.example.ms_tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;

    // Buscar ticket por ID - lanza excepción si no existe
    public Ticket findByIdOrThrow(Long ticketId) {
        log.debug("Buscando ticket por ID: {}", ticketId);
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.warn("Ticket no encontrado - ID: {}", ticketId);
                    return new TicketNotFoundException("Ticket no encontrado con ID: " + ticketId);
                });
    }

    // Crear ticket (usuario)
    public Ticket createTicket(String username, TicketDTO ticketDTO) {
        log.info("Creando ticket - Usuario: {}, Asunto: {}", username, ticketDTO.getSubject());

        Ticket ticket = Ticket.builder()
                .username(username)
                .subject(ticketDTO.getSubject())
                .description(ticketDTO.getDescription())
                .status(TicketStatus.ABIERTO)
                .createdAt(LocalDateTime.now())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket creado exitosamente - ID: {}, Usuario: {}", savedTicket.getId(), username);

        return savedTicket;
    }

    // Obtener mis tickets (usuario)
    public List<Ticket> getMyTickets(String username) {
        log.debug("Obteniendo tickets del usuario: {}", username);
        List<Ticket> tickets = ticketRepository.findByUsername(username);
        log.debug("Tickets encontrados para usuario {}: {}", username, tickets.size());
        return tickets;
    }

    // Obtener ticket por ID (verifica que sea del usuario)
    public Ticket getMyTicketById(Long ticketId, String username) {
        log.debug("Verificando ticket ID: {} para usuario: {}", ticketId, username);
        Ticket ticket = findByIdOrThrow(ticketId);

        if (!ticket.getUsername().equals(username)) {
            log.warn("Usuario {} intentó acceder al ticket de: {}", username, ticket.getUsername());
            throw new UnauthorizedTicketAccessException("No puedes ver el ticket de otro usuario");
        }

        log.debug("Acceso autorizado al ticket ID: {} para usuario: {}", ticketId, username);
        return ticket;
    }

    // Obtener todos los tickets (solo ADMIN)
    public List<Ticket> getAllTickets() {
        log.debug("Obteniendo todos los tickets - ADMIN");
        List<Ticket> tickets = ticketRepository.findAll();
        log.debug("Total de tickets encontrados: {}", tickets.size());
        return tickets;
    }

    // Obtener tickets por estado (solo ADMIN)
    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        log.debug("Obteniendo tickets por estado: {}", status);
        List<Ticket> tickets = ticketRepository.findByStatus(status);
        log.debug("Tickets con estado {} encontrados: {}", status, tickets.size());
        return tickets;
    }

    // Responder ticket (solo ADMIN)
    @Transactional
    public Ticket respondToTicket(Long ticketId, TicketResponseDTO responseDTO) {
        log.info("ADMIN - Respondiendo a ticket ID: {}", ticketId);
        Ticket ticket = findByIdOrThrow(ticketId);

        ticket.setAdminResponse(responseDTO.getAdminResponse());
        ticket.setUpdatedAt(LocalDateTime.now());

        // Si se envió un estado, actualizarlo
        if (responseDTO.getStatus() != null) {
            try {
                TicketStatus newStatus = TicketStatus.valueOf(responseDTO.getStatus().toUpperCase());
                ticket.setStatus(newStatus);
                log.debug("Ticket ID: {} - Estado actualizado a: {}", ticketId, newStatus);
            } catch (IllegalArgumentException e) {
                log.warn("Estado inválido en respuesta - Ticket ID: {}, Estado: {}", ticketId, responseDTO.getStatus());
                throw new InvalidTicketStatusException("Estado inválido. Use: ABIERTO, EN_PROCESO, RESUELTO, CERRADO");
            }
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Respuesta enviada al ticket ID: {}", ticketId);

        return savedTicket;
    }

    // Cambiar estado del ticket (solo ADMIN)
    @Transactional
    public Ticket changeTicketStatus(Long ticketId, String status) {
        log.info("ADMIN - Cambiando estado del ticket ID: {} a: {}", ticketId, status);
        Ticket ticket = findByIdOrThrow(ticketId);

        // Validar que no se pueda cambiar a RESUELTO o CERRADO sin respuesta
        if ((status.equals("RESUELTO") || status.equals("CERRADO")) &&
                (ticket.getAdminResponse() == null || ticket.getAdminResponse().isBlank())) {
            log.warn("Intento de cerrar ticket ID: {} sin respuesta previa", ticketId);
            throw new InvalidTicketStatusException("No puedes cerrar el ticket sin haber dado una respuesta");
        }

        try {
            TicketStatus newStatus = TicketStatus.valueOf(status.toUpperCase());
            ticket.setStatus(newStatus);
            ticket.setUpdatedAt(LocalDateTime.now());
            log.info("Ticket ID: {} - Estado actualizado a: {}", ticketId, newStatus);
        } catch (IllegalArgumentException e) {
            log.warn("Estado inválido solicitado - Ticket ID: {}, Estado: {}", ticketId, status);
            throw new InvalidTicketStatusException("Estado inválido. Use: ABIERTO, EN_PROCESO, RESUELTO, CERRADO");
        }

        return ticketRepository.save(ticket);
    }

    // Eliminar mi ticket (usuario)
    @Transactional
    public void deleteMyTicket(Long ticketId, String username) {
        log.warn("Eliminando ticket - ID: {}, Usuario: {}", ticketId, username);
        Ticket ticket = findByIdOrThrow(ticketId);

        if (!ticket.getUsername().equals(username)) {
            log.warn("Usuario {} intentó eliminar el ticket de: {}", username, ticket.getUsername());
            throw new UnauthorizedTicketAccessException("No puedes eliminar el ticket de otro usuario");
        }

        ticketRepository.deleteById(ticketId);
        log.info("Ticket eliminado exitosamente - ID: {}, Usuario: {}", ticketId, username);
    }

    // Eliminar cualquier ticket (solo ADMIN)
    @Transactional
    public void deleteAnyTicket(Long ticketId) {
        log.warn("ADMIN - Eliminando ticket ID: {}", ticketId);
        if (!ticketRepository.existsById(ticketId)) {
            log.warn("ADMIN - Ticket no encontrado - ID: {}", ticketId);
            throw new TicketNotFoundException("Ticket no encontrado con ID: " + ticketId);
        }
        ticketRepository.deleteById(ticketId);
        log.info("ADMIN - Ticket ID: {} eliminado exitosamente", ticketId);
    }
}