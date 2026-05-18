package com.example.ms_reviews.controller;

import com.example.ms_reviews.dto.ReviewDTO;
import com.example.ms_reviews.model.Review;
import com.example.ms_reviews.service.ReviewService;
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

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    // Crear reseña (requiere autenticación)
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReview(Authentication authentication, @Valid @RequestBody ReviewDTO reviewDTO) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de crear reseña - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado. Token inválido o faltante"));
        }

        String username = authentication.getName();
        log.info("Creando reseña - Usuario: {}, Producto: {}, Rating: {}",
                username, reviewDTO.getProductId(), reviewDTO.getRating());

        Review review = reviewService.createReview(username, reviewDTO);

        log.info("Reseña creada exitosamente - ID: {}, Usuario: {}, Producto: {}",
                review.getId(), username, reviewDTO.getProductId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Reseña creada correctamente",
                "review", Map.of(
                        "id", review.getId(),
                        "productId", review.getProductId(),
                        "rating", review.getRating(),
                        "comment", review.getComment(),
                        "productType", review.getProductType(),
                        "createdAt", review.getCreatedAt()
                )
        ));
    }

    // Obtener reseñas de un producto (público)
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable String productId) {
        log.debug("Obteniendo reseñas del producto: {}", productId);
        List<Review> reviews = reviewService.getReviewsByProduct(productId);
        log.debug("Reseñas encontradas para producto {}: {}", productId, reviews.size());
        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "total", reviews.size()
        ));
    }

    // Obtener reseñas de un producto por tipo (público)
    @GetMapping("/product/{productId}/type/{productType}")
    public ResponseEntity<?> getReviewsByProductAndType(@PathVariable String productId, @PathVariable String productType) {
        log.debug("Obteniendo reseñas - Producto: {}, Tipo: {}", productId, productType);
        List<Review> reviews = reviewService.getReviewsByProductAndType(productId, productType);
        log.debug("Reseñas encontradas: {}", reviews.size());
        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "total", reviews.size()
        ));
    }

    // Obtener mis reseñas (requiere autenticación)
    @GetMapping("/my-reviews")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de obtener mis reseñas - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Obteniendo reseñas del usuario: {}", username);

        List<Review> reviews = reviewService.getReviewsByUser(username);
        log.debug("Reseñas encontradas para usuario {}: {}", username, reviews.size());

        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "total", reviews.size()
        ));
    }

    // Actualizar mi reseña (requiere autenticación)
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateReview(Authentication authentication,
                                          @PathVariable Long reviewId,
                                          @Valid @RequestBody ReviewDTO reviewDTO) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de actualizar reseña - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Actualizando reseña - ID: {}, Usuario: {}, Nuevo rating: {}",
                reviewId, username, reviewDTO.getRating());

        Review review = reviewService.updateReview(reviewId, username, reviewDTO);

        log.info("Reseña actualizada exitosamente - ID: {}, Usuario: {}", reviewId, username);

        return ResponseEntity.ok(Map.of(
                "message", "Reseña actualizada correctamente",
                "review", review
        ));
    }

    // Eliminar mi reseña (requiere autenticación)
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteMyReview(Authentication authentication, @PathVariable Long reviewId) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Intento de eliminar reseña - Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.warn("Eliminando reseña - ID: {}, Usuario: {}", reviewId, username);

        reviewService.deleteReview(reviewId, username);

        log.info("Reseña eliminada exitosamente - ID: {}, Usuario: {}", reviewId, username);

        return ResponseEntity.ok(Map.of("message", "Tu reseña ha sido eliminada correctamente"));
    }

    // Eliminar cualquier reseña (solo ADMIN)
    @DeleteMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAnyReview(@PathVariable Long reviewId) {
        log.warn("ADMIN - Eliminando reseña - ID: {}", reviewId);
        reviewService.deleteAnyReview(reviewId);
        log.info("ADMIN - Reseña eliminada exitosamente - ID: {}", reviewId);
        return ResponseEntity.ok(Map.of("message", "Reseña eliminada correctamente por ADMIN"));
    }
}