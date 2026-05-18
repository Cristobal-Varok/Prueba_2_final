package com.example.ms_wishlist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "wishlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "productId"})
})
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;  // Usuario que agrega a wishlist (del token)

    @Column(nullable = false)
    private String productId;  // ID del producto (videojuego o comic)

    @Column(nullable = false)
    private String productName;  // Nombre del producto (para mostrar rápido)

    @Column(nullable = false)
    private String productType;  // "GAME" o "COMIC"

    @Column(nullable = false)
    private Double productPrice;  // Precio del producto

    @Column(nullable = false)
    private String imageUrl;  // Imagen del producto

    @Column(nullable = false)
    private LocalDateTime addedAt;  // Fecha en que se agregó
}
