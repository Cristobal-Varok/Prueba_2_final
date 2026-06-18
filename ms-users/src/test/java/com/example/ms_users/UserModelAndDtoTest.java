package com.example.ms_users;

import com.example.ms_users.model.User;
import com.example.ms_users.dto.UserUpdateDTO;
import com.example.ms_users.dto.UserResponseDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserModelAndDtoTest {

    @Test
    void testUserEntityGettersAndSetters() {
        User user = User.builder()
                .id(10L)
                .username("user_test")
                .mail("test@mail.com")
                .role("USER")
                .address("Av. Principal 123")
                .build();

        assertEquals(10L, user.getId());
        assertEquals("user_test", user.getUsername());
        assertEquals("test@mail.com", user.getMail());
        assertEquals("USER", user.getRole());
        assertEquals("Av. Principal 123", user.getAddress());
    }

    @Test
    void testUserUpdateDtoAndResponseData() {
        UserUpdateDTO updateDto = new UserUpdateDTO();
        updateDto.setMail("update@example.com");
        updateDto.setAddress("Nueva Direccion 456");

        UserResponseDTO responseDto = UserResponseDTO.builder()
                .id(5L)
                .username("resp_user")
                .mail("resp@mail.com")
                .build();

        assertEquals("update@example.com", updateDto.getMail());
        assertEquals("Nueva Direccion 456", updateDto.getAddress());
        assertEquals(5L, responseDto.getId());
        assertEquals("resp_user", responseDto.getUsername());
    }
}