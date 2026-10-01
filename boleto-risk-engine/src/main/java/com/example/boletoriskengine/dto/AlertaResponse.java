package com.example.boletoriskengine.dto;

import java.time.Instant;

/**
 * Bate exatamente com Alerta do alerta.model.ts no frontend.
 */
public record AlertaResponse(
        Long id,
        Long analiseRiscoId,
        String tipo,
        String descricao,
        String severidade,
        String status,
        Instant criadoEm
) {
}
