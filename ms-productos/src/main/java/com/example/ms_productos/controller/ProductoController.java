package com.example.ms_productos.controller;

import com.example.ms_productos.dto.ProductoDTO;
import com.example.ms_productos.dto.ProductoResponseDTO;
import com.example.ms_productos.service.ProductoService;
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
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<?> getAllProductos() {
        log.debug("Solicitando todos los productos");
        List<ProductoResponseDTO> productos = productoService.getAllProductos();
        return ResponseEntity.ok(Map.of(
                "productos", productos,
                "total", productos.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductoById(@PathVariable Long id) {
        log.debug("Solicitando producto por ID: {}", id);
        ProductoResponseDTO producto = productoService.getProductoById(id);
        //return ResponseEntity.ok(Map.of("producto", producto));
        return ResponseEntity.ok(producto);
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        log.info("ADMIN - Creando nuevo producto");
        ProductoResponseDTO producto = productoService.createProducto(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Producto creado correctamente",
                "producto", producto
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProducto(@PathVariable Long id, @Valid @RequestBody ProductoDTO productoDTO) {
        log.info("ADMIN - Actualizando producto ID: {}", id);
        ProductoResponseDTO producto = productoService.updateProducto(id, productoDTO);
        return ResponseEntity.ok(Map.of(
                "message", "Producto actualizado correctamente",
                "producto", producto
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProducto(@PathVariable Long id) {
        log.warn("ADMIN - Eliminando producto ID: {}", id);
        productoService.deleteProducto(id);
        return ResponseEntity.ok(Map.of("message", "Producto eliminado correctamente"));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        log.info("ADMIN - Actualizando stock - Producto ID: {}, Cantidad a descontar: {}", id, quantity);
        productoService.updateStock(id, quantity);
        return ResponseEntity.ok(Map.of("message", "Stock actualizado correctamente"));
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<?> productExists(@PathVariable Long id) {
        log.debug("Verificando existencia de producto - ID: {}", id);
        boolean exists = productoService.productExists(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}