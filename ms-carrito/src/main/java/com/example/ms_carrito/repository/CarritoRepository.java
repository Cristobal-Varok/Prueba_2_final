package com.example.ms_carrito.repository;

import com.example.ms_carrito.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}