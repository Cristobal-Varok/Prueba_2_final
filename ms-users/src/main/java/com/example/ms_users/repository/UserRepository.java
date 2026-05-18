package com.example.ms_users.repository;

import com.example.ms_users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByMail(String mail);
    List<User> findByRole(String role);// Para validar email único
    void deleteByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByMail(String mail);

}
