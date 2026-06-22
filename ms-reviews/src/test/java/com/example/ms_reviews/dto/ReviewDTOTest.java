package com.example.ms_reviews.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReviewDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void reviewDTO_ShouldBeValid_WhenAllFieldsCorrect() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("PROD-001");
        dto.setRating(5);
        dto.setComment("Excelente producto!");
        dto.setProductType("GAME");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void reviewDTO_ShouldBeInvalid_WhenProductIdBlank() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("");
        dto.setRating(5);
        dto.setComment("Comentario");
        dto.setProductType("GAME");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals("ProductId es requerido", violations.iterator().next().getMessage());
    }

    @Test
    void reviewDTO_ShouldBeInvalid_WhenRatingNull() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("PROD-001");
        dto.setRating(null);
        dto.setComment("Comentario");
        dto.setProductType("GAME");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals("Rating es requerido", violations.iterator().next().getMessage());
    }

    @Test
    void reviewDTO_ShouldBeInvalid_WhenRatingBelowMin() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("PROD-001");
        dto.setRating(0);
        dto.setComment("Comentario");
        dto.setProductType("GAME");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void reviewDTO_ShouldBeInvalid_WhenRatingAboveMax() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("PROD-001");
        dto.setRating(6);
        dto.setComment("Comentario");
        dto.setProductType("GAME");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void reviewDTO_ShouldBeInvalid_WhenCommentBlank() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("PROD-001");
        dto.setRating(5);
        dto.setComment("");
        dto.setProductType("GAME");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals("Comment es requerido", violations.iterator().next().getMessage());
    }

    @Test
    void reviewDTO_ShouldBeInvalid_WhenProductTypeInvalid() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("PROD-001");
        dto.setRating(5);
        dto.setComment("Comentario");
        dto.setProductType("INVALIDO");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals("ProductType debe ser GAME o COMIC", violations.iterator().next().getMessage());
    }

    @Test
    void reviewDTO_ShouldBeValid_WhenProductTypeIsCOMIC() {
        ReviewDTO dto = new ReviewDTO();
        dto.setProductId("PROD-001");
        dto.setRating(5);
        dto.setComment("Excelente comic!");
        dto.setProductType("COMIC");

        Set<ConstraintViolation<ReviewDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
