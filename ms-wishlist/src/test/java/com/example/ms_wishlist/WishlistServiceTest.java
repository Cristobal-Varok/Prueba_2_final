package com.example.ms_wishlist;

import com.example.ms_wishlist.client.CatalogoClient;
import com.example.ms_wishlist.client.UserClient;
import com.example.ms_wishlist.dto.WishlistDTO;
import com.example.ms_wishlist.exception.custom.WishlistItemAlreadyExistsException;
import com.example.ms_wishlist.exception.custom.WishlistItemNotFoundException;
import com.example.ms_wishlist.model.WishlistItem;
import com.example.ms_wishlist.repository.WishlistRepository;
import com.example.ms_wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private UserClient userClient;
    @Mock private CatalogoClient catalogoClient;

    @InjectMocks private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testService_AddToWishlist_Success() {
        WishlistDTO dto = new WishlistDTO();
        dto.setProductId("prod-123");
        dto.setProductName("Zelda");
        dto.setProductType("GAME");
        dto.setProductPrice(59.99);
        dto.setImageUrl("http://img.jpg");

        when(userClient.userExists("javier")).thenReturn(true);
        when(catalogoClient.productExists("prod-123")).thenReturn(true);
        when(wishlistRepository.existsByUsernameAndProductId("javier", "prod-123")).thenReturn(false);

        WishlistItem mockSaved = WishlistItem.builder()
                .id(1L)
                .username("javier")
                .productId("prod-123")
                .build();

        when(wishlistRepository.save(any(WishlistItem.class))).thenReturn(mockSaved);

        WishlistItem result = wishlistService.addToWishlist("javier", dto);

        assertNotNull(result);
        verify(wishlistRepository, times(1)).save(any(WishlistItem.class)); // Test 1
    }

    @Test
    void testService_AddToWishlist_ThrowsAlreadyExists() {
        WishlistDTO dto = new WishlistDTO();
        dto.setProductId("prod-123");

        when(userClient.userExists("javier")).thenReturn(true);
        when(catalogoClient.productExists("prod-123")).thenReturn(true);
        when(wishlistRepository.existsByUsernameAndProductId("javier", "prod-123")).thenReturn(true);

        assertThrows(WishlistItemAlreadyExistsException.class, () -> {
            wishlistService.addToWishlist("javier", dto);
        });
        verify(wishlistRepository, never()).save(any(WishlistItem.class)); // Test 2
    }

    @Test
    void testService_GetMyWishlist_WithItems() {
        List<WishlistItem> mockList = Arrays.asList(new WishlistItem(), new WishlistItem());
    }
    }