package com.example.emailscannerservice.repository;

import com.example.emailscannerservice.domain.EmailBoletoEncontrado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmailBoletoEncontradoRepository extends JpaRepository<EmailBoletoEncontrado, Long> {
    List<EmailBoletoEncontrado> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);
    Optional<EmailBoletoEncontrado> findByMensagemGmailId(String mensagemGmailId);
}
