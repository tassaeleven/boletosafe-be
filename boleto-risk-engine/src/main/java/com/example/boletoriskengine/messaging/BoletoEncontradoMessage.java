package com.example.boletoriskengine.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Formato da mensagem publicada pelo Email Scanner Service na fila
 * FILA_BOLETOS_ENCONTRADOS. Espelha o BoletoAnaliseRequest do fluxo manual,
 * com o usuarioId explicito (o Scanner ja sabe de qual usuario e o e-mail).
 */
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
