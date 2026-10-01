package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Categoria: valor e vencimento.
 * Vencimento igual a hoje ou já vencido é uma tática comum de golpe
 * (criar urgência pra a vítima não checar os dados com calma).
 */
@Component
public class RegraVencimentoProximoDemais implements RegraDeRisco {

    private static final long DIAS_MINIMOS_ACEITAVEIS = 1;

    @Override
    public String codigo() {
        return "VENCIMENTO_PROXIMO_DEMAIS";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), contexto.request().vencimentoInformado());

        if (dias < DIAS_MINIMOS_ACEITAVEIS) {
            return ResultadoAvaliacaoRegra.acionada(
                    "Vencimento igual a hoje ou já vencido — situação típica de tentativa de pressa/urgência");
        }

        return ResultadoAvaliacaoRegra.naoAcionada();
    }
}
