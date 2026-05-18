package com.example.ms_reviews.service;

import com.example.ms_reviews.client.CatalogoClient;
import com.example.ms_reviews.client.UserClient;
import com.example.ms_reviews.dto.ReviewDTO;
import com.example.ms_reviews.exception.custom.*;
import com.example.ms_reviews.model.Review;
import com.example.ms_reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserClient userClient;
    private final CatalogoClient catalogoClient;

    // Buscar reseña por ID - lanza excepción si no existe
    public Review findByIdOrThrow(Long reviewId) {
        log.debug("Buscando reseña por ID: {}", reviewId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Reseña no encontrada - ID: {}", reviewId);
                    return new ReviewNotFoundException("Reseña no encontrada con ID: " + reviewId);
                });
    }

    // Crear reseña
    public Review createReview(String username, ReviewDTO reviewDTO) {
        log.info("Creando reseña - Usuario: {}, Producto: {}", username, reviewDTO.getProductId());

        // Validar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe al crear reseña: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // Validar que el producto existe
        if (!catalogoClient.productExists(reviewDTO.getProductId())) {
            log.warn("Producto no existe - ID: {}, Usuario: {}", reviewDTO.getProductId(), username);
            throw new RuntimeException("Producto no existe con ID: " + reviewDTO.getProductId());
        }

        // Validar rating
        if (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            log.warn("Rating inválido - Usuario: {}, Rating: {}", username, reviewDTO.getRating());
            throw new InvalidRatingException("El rating debe estar entre 1 y 5 estrellas");
        }

        // Verificar si ya existe reseña del mismo usuario para el mismo producto
        if (reviewRepository.existsByProductIdAndUsername(reviewDTO.getProductId(), username)) {
            log.warn("Ya existe reseña - Usuario: {}, Producto: {}", username, reviewDTO.getProductId());
            throw new ReviewAlreadyExistsException("Ya tienes una reseña para este producto");
        }

        Review review = Review.builder()
                .username(username)
                .productId(reviewDTO.getProductId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .productType(reviewDTO.getProductType())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Reseña creada exitosamente - ID: {}, Usuario: {}, Producto: {}",
                savedReview.getId(), username, reviewDTO.getProductId());

        return savedReview;
    }

    // Obtener reseñas de un producto
    public List<Review> getReviewsByProduct(String productId) {
        log.debug("Obteniendo reseñas del producto: {}", productId);
        List<Review> reviews = reviewRepository.findByProductId(productId);
        log.debug("Reseñas encontradas para producto {}: {}", productId, reviews.size());
        return reviews;
    }

    // Obtener reseñas de un producto por tipo
    public List<Review> getReviewsByProductAndType(String productId, String productType) {
        log.debug("Obteniendo reseñas - Producto: {}, Tipo: {}", productId, productType);
        List<Review> reviews = reviewRepository.findByProductIdAndProductType(productId, productType);
        log.debug("Reseñas encontradas: {}", reviews.size());
        return reviews;
    }

    // Obtener reseñas de un usuario
    public List<Review> getReviewsByUser(String username) {
        log.debug("Obteniendo reseñas del usuario: {}", username);
        List<Review> reviews = reviewRepository.findByUsername(username);
        log.debug("Reseñas encontradas para usuario {}: {}", username, reviews.size());
        return reviews;
    }

    // Eliminar reseña propia
    public void deleteReview(Long reviewId, String username) {
        log.info("Eliminando reseña - ID: {}, Usuario: {}", reviewId, username);
        Review review = findByIdOrThrow(reviewId);

        if (!review.getUsername().equals(username)) {
            log.warn("Intento de eliminar reseña de otro usuario - Usuario: {}, Dueño: {}",
                    username, review.getUsername());
            throw new UnauthorizedReviewAccessException("No puedes eliminar la reseña de otro usuario");
        }

        reviewRepository.deleteById(reviewId);
        log.info("Reseña eliminada exitosamente - ID: {}, Usuario: {}", reviewId, username);
    }

    // Actualizar reseña propia
    public Review updateReview(Long reviewId, String username, ReviewDTO reviewDTO) {
        log.info("Actualizando reseña - ID: {}, Usuario: {}", reviewId, username);
        Review review = findByIdOrThrow(reviewId);

        if (!review.getUsername().equals(username)) {
            log.warn("Intento de modificar reseña de otro usuario - Usuario: {}, Dueño: {}",
                    username, review.getUsername());
            throw new UnauthorizedReviewAccessException("No puedes modificar la reseña de otro usuario");
        }

        // Validar rating
        if (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            log.warn("Rating inválido al actualizar - Usuario: {}, Rating: {}", username, reviewDTO.getRating());
            throw new InvalidRatingException("El rating debe estar entre 1 y 5 estrellas");
        }

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        Review updatedReview = reviewRepository.save(review);
        log.info("Reseña actualizada exitosamente - ID: {}, Usuario: {}", reviewId, username);

        return updatedReview;
    }

    // Eliminar cualquier reseña (solo ADMIN)
    public void deleteAnyReview(Long reviewId) {
        log.warn("ADMIN - Eliminando reseña - ID: {}", reviewId);
        if (!reviewRepository.existsById(reviewId)) {
            log.warn("ADMIN - Reseña no encontrada - ID: {}", reviewId);
            throw new ReviewNotFoundException("Reseña no encontrada con ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
        log.info("ADMIN - Reseña eliminada exitosamente - ID: {}", reviewId);
    }
}