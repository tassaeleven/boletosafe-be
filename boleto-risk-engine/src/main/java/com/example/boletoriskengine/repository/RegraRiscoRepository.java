package com.example.boletoriskengine.repository;

import com.example.boletoriskengine.domain.RegraRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegraRiscoRepository extends JpaRepository<RegraRisco, Long> {
    List<RegraRisco> findByAtivaTrue();
    Optional<RegraRisco> findByCodigo(String codigo);
}
