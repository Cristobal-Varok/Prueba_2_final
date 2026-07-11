package com.example.ms_reviews.controller;

import com.example.ms_reviews.dto.ReviewDTO;
import com.example.ms_reviews.model.Review;
import com.example.ms_reviews.security.filter.JwtAuthFilter;
import com.example.ms_reviews.security.jwt.JwtService;
import com.example.ms_reviews.service.CustomUserDetailsService;
import com.example.ms_reviews.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ReviewControllerTest.TestSecurityConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

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
    @WithMockUser(username = "javier", roles = {"USER"})
    void createReview_ShouldReturn201_WhenValid() throws Exception {
        when(reviewService.createReview(any(String.class), any(ReviewDTO.class))).thenReturn(review);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Reseña creada correctamente"))
                .andExpect(jsonPath("$.review.id").value(1))
                .andExpect(jsonPath("$.review.productId").value("PROD-001"))
                .andExpect(jsonPath("$.review.rating").value(5))
                .andExpect(jsonPath("$.review._links.productReviews.href").exists())
                .andExpect(jsonPath("$.review._links.myReviews.href").exists())
                .andExpect(jsonPath("$.review._links.update.href").exists())
                .andExpect(jsonPath("$.review._links.delete.href").exists());

        verify(reviewService).createReview(any(String.class), any(ReviewDTO.class));
    }

    @Test
    void createReview_ShouldReturn400_WhenInvalidData() throws Exception {
        ReviewDTO invalidDTO = new ReviewDTO();

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).createReview(any(), any());
    }

    @Test
    void getReviewsByProduct_ShouldReturn200_WhenExists() throws Exception {
        when(reviewService.getReviewsByProduct("PROD-001")).thenReturn(List.of(review));

        mockMvc.perform(get("/api/v1/reviews/product/PROD-001")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(1))
                .andExpect(jsonPath("$.reviews[0].id").value(1))
                .andExpect(jsonPath("$.reviews[0].productId").value("PROD-001"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(reviewService).getReviewsByProduct("PROD-001");
    }

    @Test
    void getReviewsByProduct_ShouldReturn200_WhenEmpty() throws Exception {
        when(reviewService.getReviewsByProduct("PROD-999")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reviews/product/PROD-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));

        verify(reviewService).getReviewsByProduct("PROD-999");
    }

    @Test
    void getReviewsByProductAndType_ShouldReturn200() throws Exception {
        when(reviewService.getReviewsByProductAndType("PROD-001", "GAME")).thenReturn(List.of(review));

        mockMvc.perform(get("/api/v1/reviews/product/PROD-001/type/GAME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(1))
                .andExpect(jsonPath("$.total").value(1));

        verify(reviewService).getReviewsByProductAndType("PROD-001", "GAME");
    }

    @Test
    @WithMockUser(username = "javier", roles = {"USER"})
    void getMyReviews_ShouldReturn200_WhenAuthenticated() throws Exception {
        when(reviewService.getReviewsByUser("javier")).thenReturn(List.of(review));

        mockMvc.perform(get("/api/v1/reviews/my-reviews")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(1))
                .andExpect(jsonPath("$.reviews[0].id").value(1))
                .andExpect(jsonPath("$.reviews[0]._links.self.href").exists())
                .andExpect(jsonPath("$.reviews[0]._links.update.href").exists())
                .andExpect(jsonPath("$.reviews[0]._links.delete.href").exists())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(reviewService).getReviewsByUser("javier");
    }

    @Test
    @WithMockUser(username = "javier", roles = {"USER"})
    void updateReview_ShouldReturn200_WhenValid() throws Exception {
        ReviewDTO updateDTO = new ReviewDTO();
        updateDTO.setProductId("PROD-001");
        updateDTO.setRating(4);
        updateDTO.setComment("Actualizado");
        updateDTO.setProductType("GAME");

        Review updatedReview = Review.builder()
                .id(1L)
                .username("javier")
                .productId("PROD-001")
                .rating(4)
                .comment("Actualizado")
                .productType("GAME")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.updateReview(eq(1L), eq("javier"), any(ReviewDTO.class))).thenReturn(updatedReview);

        mockMvc.perform(put("/api/v1/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reseña actualizada correctamente"))
                .andExpect(jsonPath("$.review.id").value(1))
                .andExpect(jsonPath("$.review.rating").value(4))
                .andExpect(jsonPath("$.review.comment").value("Actualizado"))
                .andExpect(jsonPath("$.review._links.self.href").exists())
                .andExpect(jsonPath("$.review._links.myReviews.href").exists())
                .andExpect(jsonPath("$.review._links.delete.href").exists());

        verify(reviewService).updateReview(eq(1L), eq("javier"), any(ReviewDTO.class));
    }

    @Test
    @WithMockUser(username = "javier", roles = {"USER"})
    void deleteMyReview_ShouldReturn200_WhenSuccess() throws Exception {
        doNothing().when(reviewService).deleteReview(1L, "javier");

        mockMvc.perform(delete("/api/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tu reseña ha sido eliminada correctamente"))
                .andExpect(jsonPath("$._links.myReviews.href").exists());

        verify(reviewService).deleteReview(1L, "javier");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteAnyReview_ShouldReturn200_WhenAdmin() throws Exception {
        doNothing().when(reviewService).deleteAnyReview(1L);

        mockMvc.perform(delete("/api/v1/reviews/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reseña eliminada correctamente por ADMIN"));

        verify(reviewService).deleteAnyReview(1L);
    }
}