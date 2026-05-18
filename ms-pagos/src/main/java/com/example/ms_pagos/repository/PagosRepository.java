package com.example.ms_pagos.repository;

import com.example.ms_pagos.model.Pagos;
import com.example.ms_pagos.model.PagosStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagosRepository extends JpaRepository<Pagos, Long> {
    List<Pagos> findByOrderId(Long orderId);
    List<Pagos> findByUserId(Long userId);
    List<Pagos> findByStatus(PagosStatus status);
    Optional<Pagos> findByTransactionId(String transactionId);
    boolean existsByOrderId(Long orderId);
}