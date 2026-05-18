package com.example.ms_order.client;

import com.example.ms_order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-productos")
public interface ProductClient {

    @GetMapping("/api/productos/{id}")
    ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id);
}