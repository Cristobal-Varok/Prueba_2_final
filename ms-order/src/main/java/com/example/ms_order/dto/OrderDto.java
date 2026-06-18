package com.example.ms_order.dto;

import com.example.ms_order.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderDto extends RepresentationModel<OrderDto> {
    private Long id;
    private String username;
    private OrderStatus status;
    private String paymentStatus;
    private Double totalAmount;
    private LocalDateTime createdAt;

    private List<OrderItemDto> items;
}