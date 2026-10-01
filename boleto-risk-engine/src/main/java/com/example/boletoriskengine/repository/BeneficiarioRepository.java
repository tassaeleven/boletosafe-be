package com.example.boletoriskengine.repository;

import com.example.boletoriskengine.domain.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {
    Optional<Beneficiario> findByCnpjCpf(String cnpjCpf);

    // usada pela regra de nome parecido/suspeito (RegraNomeSimilarSuspeito)
    List<Beneficiario> findAll();
}
