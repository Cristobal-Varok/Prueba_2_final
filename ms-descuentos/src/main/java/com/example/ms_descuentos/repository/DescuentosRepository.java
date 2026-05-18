package com.example.ms_descuentos.repository;

import com.example.ms_descuentos.model.Descuentos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DescuentosRepository extends JpaRepository<Descuentos, Long> {
    Optional<Descuentos> findByCode(String code);

    List<Descuentos> findByActiveTrueAndValidFromBeforeAndValidUntilAfter(LocalDateTime now, LocalDateTime now2);
}
