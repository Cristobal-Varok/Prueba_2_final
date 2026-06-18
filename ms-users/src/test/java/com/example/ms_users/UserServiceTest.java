package com.example.ms_users;

import com.example.ms_users.dto.UserUpdateDTO;
import com.example.ms_users.exception.custom.*;
import com.example.ms_users.model.User;
import com.example.ms_users.repository.UserRepository;
import com.example.ms_users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterUserSuccess() {
        User mockUser = User.builder()
                .id(1L)
                .username("juan")
                .password("encoded_pass")
                .mail("juan@mail.com")
                .role("USER")
                .build();

        when(passwordEncoder.encode("raw_pass")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User result = userService.register("juan", "raw_pass", "USER", "juan@mail.com", "Calle 123");

        assertNotNull(result);
        assertEquals("juan", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testFindByUsernameOrThrowNotFound() {
        when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findByUsernameOrThrow("fantasma"));
    }

    @Test
    void testChangeRoleInvalid() {
        User mockUser = User.builder().username("pedro").role("USER").build();
        when(userRepository.findByUsername("pedro")).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidRoleException.class, () -> userService.changeRole("pedro", "SUPER_ADMIN"));
    }

    @Test
    void testUpdateUserEmailAlreadyExistsException() {
        User existingUser = User.builder().username("juan").mail("juan@mail.com").build();
        User anotherUser = User.builder().username("maria").mail("maria@mail.com").build();
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setMail("maria@mail.com");

        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByMail("maria@mail.com")).thenReturn(Optional.of(anotherUser));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser("juan", updateDTO));
    }
}