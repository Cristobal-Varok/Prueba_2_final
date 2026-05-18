package com.example.ms_users.service;

import com.example.ms_users.dto.UserResponseDTO;
import com.example.ms_users.dto.UserUpdateDTO;
import com.example.ms_users.exception.custom.*;
import com.example.ms_users.model.User;
import com.example.ms_users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== REGISTRO ==========

    public User register(String username, String password, String role, String mail, String address) {
        log.debug("Registrando usuario - Username: {}, Email: {}, Rol: {}", username, mail, role);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .mail(mail)
                .role(role != null ? role : "USER")
                .address(address)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado exitosamente - ID: {}, Username: {}", savedUser.getId(), username);

        return savedUser;
    }

    public User register(String username, String password, String mail) {
        return register(username, password, "USER", mail, null);
    }

    // ========== BÚSQUEDAS ==========

    public Optional<User> findByUsername(String username) {
        log.debug("Buscando usuario por username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByMail(String mail) {
        log.debug("Buscando usuario por email: {}", mail);
        return userRepository.findByMail(mail);
    }

    public User findByUsernameOrThrow(String username) {
        log.debug("Buscando usuario por username (con excepción): {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", username);
                    return new UserNotFoundException("Usuario no encontrado: " + username);
                });
    }

    // ========== OBTENER PERFILES (DTO) ==========

    public List<UserResponseDTO> getAllUsers() {
        log.debug("Obteniendo todos los usuarios");
        List<UserResponseDTO> users = userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.debug("Total de usuarios encontrados: {}", users.size());
        return users;
    }

    public UserResponseDTO getUserProfile(String username) {
        log.debug("Obteniendo perfil de usuario: {}", username);
        User user = findByUsernameOrThrow(username);
        return convertToDTO(user);
    }

    public List<UserResponseDTO> getUsersByRole(String role) {
        log.debug("Obteniendo usuarios por rol: {}", role);
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            log.warn("Rol inválido solicitado: {}", role);
            throw new InvalidRoleException("Rol inválido. Use: USER o ADMIN");
        }
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Obteniendo usuario por email: {}", email);
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado por email: {}", email);
                    return new UserNotFoundException("Usuario no encontrado con email: " + email);
                });
        return convertToDTO(user);
    }

    // ========== ACTUALIZACIONES ==========

    @Transactional
    public UserResponseDTO updateUser(String username, UserUpdateDTO updateDTO) {
        log.info("Actualizando usuario: {}", username);
        User user = findByUsernameOrThrow(username);

        if (updateDTO.getMail() != null && !updateDTO.getMail().isBlank()) {
            Optional<User> existingUser = userRepository.findByMail(updateDTO.getMail());
            if (existingUser.isPresent() && !existingUser.get().getUsername().equals(username)) {
                log.warn("Intento de usar email ya existente: {} para usuario: {}", updateDTO.getMail(), username);
                throw new EmailAlreadyExistsException("El email '" + updateDTO.getMail() + "' ya está en uso por otro usuario");
            }
            log.debug("Actualizando email de {} a: {}", username, updateDTO.getMail());
            user.setMail(updateDTO.getMail());
        }

        if (updateDTO.getAddress() != null) {
            log.debug("Actualizando dirección de {} a: {}", username, updateDTO.getAddress());
            user.setAddress(updateDTO.getAddress());
        }

        User savedUser = userRepository.save(user);
        log.info("Usuario actualizado exitosamente: {}", username);
        return convertToDTO(savedUser);
    }

    @Transactional
    public UserResponseDTO updateAddress(String username, String newAddress) {
        log.info("Actualizando dirección del usuario: {}", username);
        User user = findByUsernameOrThrow(username);
        user.setAddress(newAddress);
        User savedUser = userRepository.save(user);
        log.info("Dirección actualizada exitosamente para: {}", username);
        return convertToDTO(savedUser);
    }

    // ========== CAMBIAR ROL ==========

    @Transactional
    public UserResponseDTO changeRole(String username, String newRole) {
        log.info("Cambiando rol del usuario: {} a: {}", username, newRole);
        User user = findByUsernameOrThrow(username);

        if (!newRole.equals("USER") && !newRole.equals("ADMIN")) {
            log.warn("Intento de cambiar rol inválido: {} para usuario: {}", newRole, username);
            throw new InvalidRoleException("Rol inválido. Use: USER o ADMIN");
        }

        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        log.info("Rol cambiado exitosamente para: {} -> {}", username, newRole);
        return convertToDTO(savedUser);
    }

    // ========== ELIMINAR ==========

    @Transactional
    public void deleteByUsername(String username) throws Exception {
        log.warn("Eliminando usuario: {}", username);
        if (!userRepository.existsByUsername(username)) {
            log.warn("Intento de eliminar usuario inexistente: {}", username);
            throw new UserNotFoundException("Usuario no encontrado: " + username);
        }
        userRepository.deleteByUsername(username);
        log.info("Usuario eliminado exitosamente: {}", username);
    }

    @Transactional
    public void deleteOwnAccount(String username) throws Exception {
        log.warn("Eliminando propia cuenta: {}", username);
        deleteByUsername(username);
    }

    // ========== CONVERSIÓN ==========

    private UserResponseDTO convertToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .mail(user.getMail())
                .role(user.getRole())
                .address(user.getAddress())
                .build();
    }
}