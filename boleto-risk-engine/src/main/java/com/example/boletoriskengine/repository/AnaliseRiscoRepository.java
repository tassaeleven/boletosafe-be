package com.example.boletoriskengine.repository;

import com.example.boletoriskengine.domain.AnaliseRisco;
import com.example.boletoriskengine.domain.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface AnaliseRiscoRepository extends JpaRepository<AnaliseRisco, Long> {

    List<AnaliseRisco> findByBeneficiario(Beneficiario beneficiario);

    // usada pela regra de valor fora da media historica do beneficiario
    @org.springframework.data.jpa.repository.Query(
        "select avg(a.valorInformado) from AnaliseRisco a where a.beneficiario = :beneficiario"
    )
    BigDecimal mediaValorPorBeneficiario(@Param("beneficiario") Beneficiario beneficiario);
}
