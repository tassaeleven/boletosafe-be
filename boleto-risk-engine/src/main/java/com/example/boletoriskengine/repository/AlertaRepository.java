package com.example.boletoriskengine.repository;

import com.example.boletoriskengine.domain.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByStatus(String status);

    // usada pela regra de reincidencia: beneficiario ja teve alerta ALTO?
    @org.springframework.data.jpa.repository.Query("""
        select count(al) > 0 from Alerta al
        where al.analiseRisco.beneficiario.id = :beneficiarioId
        and al.severidade = 'ALTA'
    """)
    boolean existeAlertaAltoParaBeneficiario(@Param("beneficiarioId") Long beneficiarioId);
}
