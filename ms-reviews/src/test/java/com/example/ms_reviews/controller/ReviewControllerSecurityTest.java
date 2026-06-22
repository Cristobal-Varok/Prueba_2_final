package com.example.ms_reviews.controller;

import com.example.ms_reviews.dto.ReviewDTO;
import com.example.ms_reviews.security.filter.JwtAuthFilter;
import com.example.ms_reviews.security.jwt.JwtService;
import com.example.ms_reviews.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc
class ReviewControllerSecurityTest {

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

    @Test
    void createReview_ShouldReturn401_WhenNoToken() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setProductId("PROD-001");
        reviewDTO.setRating(5);
        reviewDTO.setComment("Comentario");
        reviewDTO.setProductType("GAME");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyReviews_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/my-reviews"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateReview_ShouldReturn401_WhenNoToken() throws Exception {
        ReviewDTO updateDTO = new ReviewDTO();
        updateDTO.setProductId("PROD-001");
        updateDTO.setRating(4);
        updateDTO.setComment("Actualizado");
        updateDTO.setProductType("GAME");

        mockMvc.perform(put("/api/v1/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMyReview_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAnyReview_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/admin/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReviewsByProduct_ShouldReturn200_WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/product/PROD-001"))
                .andExpect(status().isOk());
    }

    @Test
    void getReviewsByProductAndType_ShouldReturn200_WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/product/PROD-001/type/GAME"))
                .andExpect(status().isOk());
    }
}