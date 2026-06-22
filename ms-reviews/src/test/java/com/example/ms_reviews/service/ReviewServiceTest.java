package com.example.ms_reviews.service;

import com.example.ms_reviews.client.CatalogoClient;
import com.example.ms_reviews.client.UserClient;
import com.example.ms_reviews.dto.ReviewDTO;
import com.example.ms_reviews.exception.custom.*;
import com.example.ms_reviews.model.Review;
import com.example.ms_reviews.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        reviewDTO = new ReviewDTO();
        reviewDTO.setProductId("PROD-001");
        reviewDTO.setRating(5);
        reviewDTO.setComment("Excelente producto!");
        reviewDTO.setProductType("GAME");

        review = Review.builder()
                .id(1L)
                .username("javier")
                .productId("PROD-001")
                .rating(5)
                .comment("Excelente producto!")
                .productType("GAME")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByIdOrThrow_ShouldReturnReview_WhenExists() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Review result = reviewService.findByIdOrThrow(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reviewRepository).findById(1L);
    }

    @Test
    void findByIdOrThrow_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.findByIdOrThrow(99L));
        verify(reviewRepository).findById(99L);
    }

    @Test
    void createReview_ShouldCreateReview_WhenValid() {
        when(userClient.userExists("javier")).thenReturn(true);
        when(catalogoClient.productExists("PROD-001")).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUsername("PROD-001", "javier")).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.createReview("javier", reviewDTO);

        assertNotNull(result);
        assertEquals("PROD-001", result.getProductId());
        assertEquals(5, result.getRating());
        verify(userClient).userExists("javier");
        verify(catalogoClient).productExists("PROD-001");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenUserNotExists() {
        when(userClient.userExists("javier")).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> reviewService.createReview("javier", reviewDTO));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_ShouldThrowException_WhenProductNotExists() {
        when(userClient.userExists("javier")).thenReturn(true);
        when(catalogoClient.productExists("PROD-001")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> reviewService.createReview("javier", reviewDTO));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_ShouldThrowException_WhenRatingInvalid() {
        reviewDTO.setRating(6);
        when(userClient.userExists("javier")).thenReturn(true);
        when(catalogoClient.productExists("PROD-001")).thenReturn(true);

        assertThrows(InvalidRatingException.class, () -> reviewService.createReview("javier", reviewDTO));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_ShouldThrowException_WhenReviewAlreadyExists() {
        when(userClient.userExists("javier")).thenReturn(true);
        when(catalogoClient.productExists("PROD-001")).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUsername("PROD-001", "javier")).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.createReview("javier", reviewDTO));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewsByProduct_ShouldReturnList() {
        when(reviewRepository.findByProductId("PROD-001")).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByProduct("PROD-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByProductId("PROD-001");
    }

    @Test
    void getReviewsByProduct_ShouldReturnEmptyList_WhenNoReviews() {
        when(reviewRepository.findByProductId("PROD-999")).thenReturn(List.of());

        List<Review> result = reviewService.getReviewsByProduct("PROD-999");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewRepository).findByProductId("PROD-999");
    }

    @Test
    void getReviewsByProductAndType_ShouldReturnList() {
        when(reviewRepository.findByProductIdAndProductType("PROD-001", "GAME")).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByProductAndType("PROD-001", "GAME");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByProductIdAndProductType("PROD-001", "GAME");
    }

    @Test
    void getReviewsByUser_ShouldReturnList() {
        when(reviewRepository.findByUsername("javier")).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByUser("javier");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByUsername("javier");
    }

    @Test
    void deleteReview_ShouldDelete_WhenUserOwnsReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(1L, "javier");

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_ShouldThrowException_WhenUserDoesNotOwnReview() {
        Review otherUserReview = Review.builder().id(2L).username("pedro").build();
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(otherUserReview));

        assertThrows(UnauthorizedReviewAccessException.class, () -> reviewService.deleteReview(2L, "javier"));
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    void updateReview_ShouldUpdate_WhenUserOwnsReview() {
        ReviewDTO updateDTO = new ReviewDTO();
        updateDTO.setRating(4);
        updateDTO.setComment("Buen producto");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.updateReview(1L, "javier", updateDTO);

        assertNotNull(result);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateReview_ShouldThrowException_WhenRatingInvalid() {
        ReviewDTO invalidDTO = new ReviewDTO();
        invalidDTO.setRating(6);
        invalidDTO.setComment("Comentario");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(InvalidRatingException.class, () -> reviewService.updateReview(1L, "javier", invalidDTO));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteAnyReview_ShouldDelete_WhenAdmin() {
        when(reviewRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteAnyReview(1L);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteAnyReview_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.existsById(99L)).thenReturn(false);

        assertThrows(ReviewNotFoundException.class, () -> reviewService.deleteAnyReview(99L));
        verify(reviewRepository, never()).deleteById(any());
    }
}