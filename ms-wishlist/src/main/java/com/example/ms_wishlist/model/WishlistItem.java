package com.example.ms_wishlist.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "wishlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "productId"})
})
@Schema(description = "Entidad que representa un producto en la lista de deseos de un usuario")
public class WishlistItem extends RepresentationModel<WishlistItem> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del item en wishlist", example = "1", accessMode = READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre del usuario que agregó el producto", example = "javier")
    private String username;

    @Column(nullable = false)
    @Schema(description = "ID del producto (videojuego o comic)", example = "prod-123")
    private String productId;

    @Column(nullable = false)
    @Schema(description = "Nombre del producto", example = "The Legend of Zelda")
    private String productName;

    @Column(nullable = false)
    @Schema(description = "Tipo de producto", example = "GAME", allowableValues = {"GAME", "COMIC"})
    private String productType;

    @Column(nullable = false)
    @Schema(description = "Precio del producto", example = "59.99")
    private Double productPrice;

    @Column(nullable = false)
    @Schema(description = "URL de la imagen del producto", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Column(nullable = false)
    @Schema(description = "Fecha en que se agregó a la wishlist", example = "2026-06-08T10:30:00", accessMode = READ_ONLY)
    private LocalDateTime addedAt;
}