package com.example.ms_reviews.controller;

import com.example.ms_reviews.dto.ReviewDTO;
import com.example.ms_reviews.model.Review;
import com.example.ms_reviews.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reseñas", description = "Endpoints para gestionar reseñas de productos")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    @Operation(summary = "Crear reseña", description = "Crea una nueva reseña para un producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reseña creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario o producto no existe"),
            @ApiResponse(responseCode = "409", description = "Ya existe reseña para este producto")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReview(Authentication authentication, @Valid @RequestBody ReviewDTO reviewDTO) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Creando reseña - Usuario: {}, Producto: {}, Rating: {}", username, reviewDTO.getProductId(), reviewDTO.getRating());

        Review review = reviewService.createReview(username, reviewDTO);

        review.add(linkTo(methodOn(ReviewController.class).getReviewsByProduct(reviewDTO.getProductId())).withRel("productReviews"));
        review.add(linkTo(methodOn(ReviewController.class).getMyReviews(authentication)).withRel("myReviews"));
        review.add(linkTo(methodOn(ReviewController.class).updateReview(authentication, review.getId(), null)).withRel("update"));
        review.add(linkTo(methodOn(ReviewController.class).deleteMyReview(authentication, review.getId())).withRel("delete"));

        log.info("Reseña creada exitosamente - ID: {}, Usuario: {}", review.getId(), username);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Reseña creada correctamente",
                "review", review
        ));
    }

    @Operation(summary = "Obtener reseñas de un producto", description = "Devuelve todas las reseñas de un producto (público)")
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable String productId) {
        log.debug("Obteniendo reseñas del producto: {}", productId);
        List<Review> reviews = reviewService.getReviewsByProduct(productId);

        reviews.forEach(review -> {
            review.add(linkTo(methodOn(ReviewController.class).getReviewsByProduct(productId)).withSelfRel());
        });

        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "total", reviews.size(),
                "_links", Map.of("self", linkTo(methodOn(ReviewController.class).getReviewsByProduct(productId)).withSelfRel())
        ));
    }

    @Operation(summary = "Obtener reseñas por producto y tipo", description = "Devuelve reseñas filtradas por producto y tipo (público)")
    @GetMapping("/product/{productId}/type/{productType}")
    public ResponseEntity<?> getReviewsByProductAndType(@PathVariable String productId, @PathVariable String productType) {
        log.debug("Obteniendo reseñas - Producto: {}, Tipo: {}", productId, productType);
        List<Review> reviews = reviewService.getReviewsByProductAndType(productId, productType);

        reviews.forEach(review -> {
            review.add(linkTo(methodOn(ReviewController.class).getReviewsByProductAndType(productId, productType)).withSelfRel());
        });

        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "total", reviews.size()
        ));
    }

    @Operation(summary = "Obtener mis reseñas", description = "Devuelve todas las reseñas del usuario autenticado")
    @GetMapping("/my-reviews")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.debug("Obteniendo reseñas del usuario: {}", username);

        List<Review> reviews = reviewService.getReviewsByUser(username);

        reviews.forEach(review -> {
            review.add(linkTo(methodOn(ReviewController.class).getMyReviews(authentication)).withSelfRel());
            review.add(linkTo(methodOn(ReviewController.class).updateReview(authentication, review.getId(), null)).withRel("update"));
            review.add(linkTo(methodOn(ReviewController.class).deleteMyReview(authentication, review.getId())).withRel("delete"));
        });

        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "total", reviews.size(),
                "_links", Map.of("self", linkTo(methodOn(ReviewController.class).getMyReviews(authentication)).withSelfRel())
        ));
    }

    @Operation(summary = "Actualizar mi reseña", description = "Actualiza una reseña existente del usuario autenticado")
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateReview(Authentication authentication,
                                          @PathVariable Long reviewId,
                                          @Valid @RequestBody ReviewDTO reviewDTO) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.info("Actualizando reseña - ID: {}, Usuario: {}", reviewId, username);

        Review review = reviewService.updateReview(reviewId, username, reviewDTO);

        review.add(linkTo(methodOn(ReviewController.class).updateReview(authentication, reviewId, null)).withSelfRel());
        review.add(linkTo(methodOn(ReviewController.class).getMyReviews(authentication)).withRel("myReviews"));
        review.add(linkTo(methodOn(ReviewController.class).deleteMyReview(authentication, reviewId)).withRel("delete"));

        log.info("Reseña actualizada exitosamente - ID: {}", reviewId);

        return ResponseEntity.ok(Map.of(
                "message", "Reseña actualizada correctamente",
                "review", review
        ));
    }

    @Operation(summary = "Eliminar mi reseña", description = "Elimina una reseña del usuario autenticado")
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteMyReview(Authentication authentication, @PathVariable Long reviewId) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        log.warn("Eliminando reseña - ID: {}, Usuario: {}", reviewId, username);

        reviewService.deleteReview(reviewId, username);

        log.info("Reseña eliminada exitosamente - ID: {}", reviewId);

        return ResponseEntity.ok(Map.of(
                "message", "Tu reseña ha sido eliminada correctamente",
                "_links", Map.of("myReviews", linkTo(methodOn(ReviewController.class).getMyReviews(authentication)).withRel("myReviews"))
        ));
    }

    @Operation(summary = "Eliminar cualquier reseña (ADMIN)", description = "Elimina cualquier reseña del sistema (solo ADMIN)")
    @DeleteMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAnyReview(@PathVariable Long reviewId) {
        log.warn("ADMIN - Eliminando reseña - ID: {}", reviewId);
        reviewService.deleteAnyReview(reviewId);
        log.info("ADMIN - Reseña eliminada exitosamente - ID: {}", reviewId);
        return ResponseEntity.ok(Map.of("message", "Reseña eliminada correctamente por ADMIN"));
    }
}