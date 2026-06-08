package com.example.ms_reviews.repository;

import com.example.ms_reviews.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(String productId);
    List<Review> findByUsername(String username);
    List<Review> findByProductIdAndProductType(String productId, String productType);
    boolean existsByProductIdAndUsername(String productId, String username);
    //obtener review por id falla
}