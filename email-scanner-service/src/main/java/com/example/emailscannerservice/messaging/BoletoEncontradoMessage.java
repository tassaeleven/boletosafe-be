package com.example.emailscannerservice.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Precisa ter os MESMOS campos do record equivalente no boleto-risk-engine. */
public record BoletoEncontradoMessage(
        Long usuarioId,
        String linhaDigitavel,
        String beneficiarioNome,
        String beneficiarioDocumento,
        BigDecimal valorInformado,
        LocalDate vencimentoInformado,
        Long bancoInstituicaoId
) {
}
