package com.example.ms_order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItemDto extends RepresentationModel<OrderItemDto> {
    private Long id;
    private Integer quantity;
    private Double price;


    private ProductDto product;
}