package com.example.ms_order.controller;

import com.example.ms_order.dto.OrderDto;
import com.example.ms_order.dto.OrderItemDto;
import com.example.ms_order.model.Order;
import com.example.ms_order.model.enums.OrderStatus;
import com.example.ms_order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "API para la gestión de órdenes de compra")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    /**
     * Convierte una entidad Order a OrderDto con enlaces HATEOAS
     */
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUsername(order.getUsername());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());

        // Convertir items
        if (order.getItems() != null) {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                    .map(item -> {
                        OrderItemDto itemDto = new OrderItemDto();
                        itemDto.setId(item.getId());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setPrice(item.getPrice());
                        itemDto.setProductId(item.getProductId());
                        // Link al producto (enlace externo)
                        return itemDto;
                    })
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        // ========== ENLACES HATEOAS ==========

        // Self link
        dto.add(linkTo(methodOn(OrderController.class).getOrderById(order.getId())).withSelfRel());

        // Link a todas las órdenes
        dto.add(linkTo(methodOn(OrderController.class).getAllOrders()).withRel("orders"));

        // Links para cambiar estado
        dto.add(linkTo(methodOn(OrderController.class)
                .changeOrderStatus(order.getId(), OrderStatus.PAID))
                .withRel("markAsPaid"));

        dto.add(linkTo(methodOn(OrderController.class)
                .changeOrderStatus(order.getId(), OrderStatus.PROCESSING))
                .withRel("markAsProcessing"));

        dto.add(linkTo(methodOn(OrderController.class)
                .changeOrderStatus(order.getId(), OrderStatus.SHIPPED))
                .withRel("markAsShipped"));

        dto.add(linkTo(methodOn(OrderController.class)
                .changeOrderStatus(order.getId(), OrderStatus.DELIVERED))
                .withRel("markAsDelivered"));

        dto.add(linkTo(methodOn(OrderController.class)
                .changeOrderStatus(order.getId(), OrderStatus.CANCELLED))
                .withRel("cancel"));

        // Links para actualizar pago
        dto.add(linkTo(methodOn(OrderController.class)
                .updatePaymentStatus(order.getId(), "PAID"))
                .withRel("paymentPaid"));

        dto.add(linkTo(methodOn(OrderController.class)
                .updatePaymentStatus(order.getId(), "PAYMENT_FAILED"))
                .withRel("paymentFailed"));

        // Link para actualizar
        dto.add(linkTo(methodOn(OrderController.class)
                .updateOrder(order.getId(), null))
                .withRel("update"));

        // Link para eliminar (solo si es posible)
        if (order.getStatus() == OrderStatus.PENDING ||
                order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            dto.add(linkTo(methodOn(OrderController.class).deleteOrder(order.getId()))
                    .withRel("delete"));
        }

        // Link para verificar existencia
        dto.add(linkTo(methodOn(OrderController.class).orderExists(order.getId()))
                .withRel("exists"));

        // Link para órdenes del mismo estado
        dto.add(linkTo(methodOn(OrderController.class)
                .getOrdersByStatus(order.getStatus()))
                .withRel("sameStatus"));

        // Link para crear nueva orden
        dto.add(linkTo(methodOn(OrderController.class).createOrder(null))
                .withRel("create"));

        return dto;
    }

    @Operation(
            summary = "Crear una nueva orden",
            description = "Registra una nueva orden en el sistema con sus ítems asociados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden creada correctamente",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<OrderDto> createOrder(
            @Parameter(description = "Datos de la orden a crear", required = true)
            @Valid @RequestBody Order order) {
        log.info("Creando nueva orden");
        Order created = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToDto(created));
    }

    @Operation(
            summary = "Obtener todas las órdenes",
            description = "Retorna una lista de todas las órdenes registradas"
    )
    @ApiResponse(responseCode = "200", description = "Órdenes obtenidas correctamente")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CollectionModel<OrderDto>> getAllOrders() {
        log.info("Obteniendo todas las órdenes");
        List<Order> orders = orderService.getAllOrders();

        // Convertir cada orden a DTO con enlaces
        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // Crear colección con enlaces adicionales
        CollectionModel<OrderDto> collection = CollectionModel.of(orderDtos);

        // Enlaces de la colección
        collection.add(linkTo(methodOn(OrderController.class).getAllOrders()).withSelfRel());
        collection.add(linkTo(methodOn(OrderController.class).getOrdersByStatus(OrderStatus.PENDING)).withRel("pending"));
        collection.add(linkTo(methodOn(OrderController.class).getOrdersByStatus(OrderStatus.PAID)).withRel("paid"));
        collection.add(linkTo(methodOn(OrderController.class).getOrdersByStatus(OrderStatus.PROCESSING)).withRel("processing"));
        collection.add(linkTo(methodOn(OrderController.class).getOrdersByStatus(OrderStatus.SHIPPED)).withRel("shipped"));
        collection.add(linkTo(methodOn(OrderController.class).getOrdersByStatus(OrderStatus.DELIVERED)).withRel("delivered"));
        collection.add(linkTo(methodOn(OrderController.class).getOrdersByStatus(OrderStatus.CANCELLED)).withRel("cancelled"));
        collection.add(linkTo(methodOn(OrderController.class).createOrder(null)).withRel("create"));

        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Obtener órdenes por estado",
            description = "Filtra las órdenes según su estado actual"
    )
    @ApiResponse(responseCode = "200", description = "Órdenes filtradas correctamente")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CollectionModel<OrderDto>> getOrdersByStatus(
            @Parameter(description = "Estado de la orden (PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED)", required = true)
            @PathVariable OrderStatus status) {
        log.info("Obteniendo órdenes por estado: {}", status);
        List<Order> orders = orderService.getOrdersByStatus(status);

        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        CollectionModel<OrderDto> collection = CollectionModel.of(orderDtos);

        collection.add(linkTo(methodOn(OrderController.class).getOrdersByStatus(status)).withSelfRel());
        collection.add(linkTo(methodOn(OrderController.class).getAllOrders()).withRel("allOrders"));

        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Obtener órdenes por cliente",
            description = "Retorna todas las órdenes de un cliente específico"
    )
    @ApiResponse(responseCode = "200", description = "Órdenes del cliente obtenidas correctamente")
    @GetMapping("/client/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CollectionModel<OrderDto>> getOrdersByClient(
            @Parameter(description = "Nombre de usuario del cliente", required = true)
            @PathVariable String username) {
        log.info("Obteniendo órdenes del cliente: {}", username);
        List<Order> orders = orderService.getOrdersByUsername(username);

        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        CollectionModel<OrderDto> collection = CollectionModel.of(orderDtos);

        collection.add(linkTo(methodOn(OrderController.class).getOrdersByClient(username)).withSelfRel());
        collection.add(linkTo(methodOn(OrderController.class).getAllOrders()).withRel("allOrders"));

        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Obtener una orden por ID",
            description = "Retorna los detalles de una orden específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden encontrada",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "ID de la orden", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Obteniendo orden por ID: {}", id);
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(convertToDto(order));
    }

    @Operation(
            summary = "Verificar existencia de una orden",
            description = "Endpoint público para verificar si una orden existe en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden existe"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/exists/{id}")
    public ResponseEntity<OrderDto> orderExists(
            @Parameter(description = "ID de la orden", required = true, example = "1")
            @PathVariable Long id) {
        log.debug("Verificando existencia de orden con ID: {}", id);
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(convertToDto(order));
    }

    @Operation(
            summary = "Actualizar una orden",
            description = "Actualiza los datos de una orden existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden actualizada correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<OrderDto> updateOrder(
            @Parameter(description = "ID de la orden", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la orden", required = true)
            @Valid @RequestBody Order orderData) {
        log.info("Actualizando orden ID: {}", id);
        Order updated = orderService.updateOrder(id, orderData);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @Operation(
            summary = "Cambiar estado de una orden",
            description = "Actualiza el estado de una orden (PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<OrderDto> changeOrderStatus(
            @Parameter(description = "ID de la orden", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la orden", required = true)
            @RequestParam OrderStatus status) {
        log.info("Cambiando estado de orden ID: {} a {}", id, status);
        Order updated = orderService.changeOrderStatus(id, status);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @Operation(
            summary = "Actualizar estado de pago",
            description = "Endpoint público para actualizar el estado de pago de una orden"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de pago actualizado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<OrderDto> updatePaymentStatus(
            @Parameter(description = "ID de la orden", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Estado de pago (PENDING, PAID, PAYMENT_FAILED)", required = true)
            @RequestParam String status) {
        log.info("Actualizando estado de pago de orden ID: {} a {}", id, status);
        Order updated = orderService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @Operation(
            summary = "Eliminar una orden",
            description = "Elimina una orden del sistema (solo si está en estado PENDING, CANCELLED o PAYMENT_FAILED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orden eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar la orden en su estado actual")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID de la orden a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Eliminando orden ID: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}