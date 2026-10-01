package com.example.boletoriskengine.repository;

import com.example.boletoriskengine.domain.BancoInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BancoInstituicaoRepository extends JpaRepository<BancoInstituicao, Long> {
    Optional<BancoInstituicao> findByCodigoFebraban(String codigoFebraban);
}
