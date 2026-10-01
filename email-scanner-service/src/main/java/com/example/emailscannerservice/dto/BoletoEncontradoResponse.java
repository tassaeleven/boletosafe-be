package com.example.emailscannerservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BoletoEncontradoResponse(
        Long id,
        String remetente,
        String assunto,
        String nomeArquivoAnexo,
        String status,
        String beneficiarioNomeExtraido,
        BigDecimal valorExtraido,
        LocalDate vencimentoExtraido,
        Instant criadoEm
) {
}
