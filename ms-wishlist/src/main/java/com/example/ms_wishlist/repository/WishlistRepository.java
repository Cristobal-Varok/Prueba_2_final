package com.example.ms_wishlist.repository;

import com.example.ms_wishlist.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUsername(String username);

    Optional<WishlistItem> findByUsernameAndProductId(String username, String productId);

    boolean existsByUsernameAndProductId(String username, String productId);

    void deleteByUsernameAndProductId(String username, String productId);

    long countByUsername(String username);
}