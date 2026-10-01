package com.example.boletoriskengine.repository;

import com.example.boletoriskengine.domain.OrigemCobranca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrigemCobrancaRepository extends JpaRepository<OrigemCobranca, Long> {
    Optional<OrigemCobranca> findByCodigo(String codigo);
}
