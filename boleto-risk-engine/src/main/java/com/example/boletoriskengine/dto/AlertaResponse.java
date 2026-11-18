package com.example.boletoriskengine.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Bate exatamente com Alerta do alerta.model.ts no frontend.
 * Além do alerta, leva os dados do boleto analisado para a tela agrupar por boleto.
 */
public record AlertaResponse(
        Long id,
        Long analiseRiscoId,
        String tipo,
        String descricao,
        String severidade,
        String status,
        Instant criadoEm,
        String linhaDigitavel,
        String beneficiarioNome,
        String beneficiarioDocumento,
        BigDecimal valor,
        LocalDate vencimento,
        int scoreTotal,
        String nivelRisco,
        String titulo
) {
}