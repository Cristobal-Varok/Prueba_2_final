package com.example.ms_envios.repository;

import com.example.ms_envios.model.Envios;
import com.example.ms_envios.model.EnviosStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnviosRepository extends JpaRepository<Envios, Long> {
    List<Envios> findByOrderId(Long orderId);
    List<Envios> findByUserId(Long userId);
    List<Envios> findByStatus(EnviosStatus status);
    Optional<Envios> findByOrderIdAndStatusNot(Long orderId, EnviosStatus status);
    boolean existsByOrderId(Long orderId);
}