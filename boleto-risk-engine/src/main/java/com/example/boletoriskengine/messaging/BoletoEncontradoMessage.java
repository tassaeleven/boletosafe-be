package com.example.boletoriskengine.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BoletoEncontradoMessage(
        Long usuarioId,
        String linhaDigitavel,
        String beneficiarioNome,
        String beneficiarioDocumento,
        BigDecimal valorInformado,
        LocalDate vencimentoInformado,
        Long bancoInstituicaoId,
        String assunto
) {
}