package com.example.ms_reviews.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;  // Usuario que escribe la reseña (del token)

    @Column(nullable = false)
    private String productId;  // ID del producto (videojuego o comic)

    @Column(nullable = false)
    private Integer rating;  // 1 a 5 estrellas

    @Column(nullable = false, length = 1000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String productType;  // "GAME" o "COMIC"
}