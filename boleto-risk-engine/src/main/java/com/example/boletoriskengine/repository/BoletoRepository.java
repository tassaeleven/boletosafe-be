package com.example.boletoriskengine.repository;

import com.example.boletoriskengine.domain.Boleto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BoletoRepository extends JpaRepository<Boleto, Long> {
    Optional<Boleto> findByLinhaDigitavel(String linhaDigitavel);
}
