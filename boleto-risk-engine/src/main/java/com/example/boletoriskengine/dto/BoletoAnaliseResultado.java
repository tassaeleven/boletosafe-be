package com.example.boletoriskengine.dto;

import java.time.Instant;

/**
 * Bate exatamente com BoletoAnaliseResultado do boleto.model.ts no frontend.
 */
public record BoletoAnaliseResultado(
        Long id,
        int scoreTotal,
        String nivelRisco,
        String observacoes,
        Instant criadoEm
) {
}
