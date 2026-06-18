package com.example.ms_users;

import com.example.ms_users.model.User;
import com.example.ms_users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
    }

    @Test
    void testFindByUsernameAndMail() {
        User mockUser = User.builder().username("dev_user").mail("dev@mail.com").build();

        when(userRepository.findByUsername("dev_user")).thenReturn(Optional.of(mockUser));
        when(userRepository.findByMail("dev@mail.com")).thenReturn(Optional.of(mockUser));

        Optional<User> foundUsername = userRepository.findByUsername("dev_user");
        Optional<User> foundMail = userRepository.findByMail("dev@mail.com");

        assertThat(foundUsername).isPresent();
        assertThat(foundMail).isPresent();
        assertThat(foundUsername.get().getUsername()).isEqualTo("dev_user");
    }

    @Test
    void testFindByRole() {
        User u1 = User.builder().username("admin1").role("ADMIN").build();
        when(userRepository.findByRole("ADMIN")).thenReturn(List.of(u1));

        List<User> admins = userRepository.findByRole("ADMIN");

        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getRole()).isEqualTo("ADMIN");
    }

    @Test
    void testExistsByUsernameAndMail() {
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);
        when(userRepository.existsByMail("existing@mail.com")).thenReturn(true);

        boolean userExists = userRepository.existsByUsername("existingUser");
        boolean mailExists = userRepository.existsByMail("existing@mail.com");

        assertThat(userExists).isTrue();
        assertThat(mailExists).isTrue();
    }
}