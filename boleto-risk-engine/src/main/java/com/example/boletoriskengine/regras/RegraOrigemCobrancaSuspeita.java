package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Categoria: origem da cobranca.
 * Boletos recebidos por canais informais (e-mail, WhatsApp, SMS) pesam
 * mais que canais oficiais (portal do proprio fornecedor, carta fisica),
 * porque sao os canais mais faceis de forjar/interceptar.
 */
@Component
public class RegraOrigemCobrancaSuspeita implements RegraDeRisco {

    private static final Set<String> CANAIS_SUSPEITOS = Set.of("EMAIL", "WHATSAPP", "SMS");

    @Override
    public String codigo() {
        return "ORIGEM_COBRANCA_SUSPEITA";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        if (contexto.origemCobranca() == null) {
            return ResultadoAvaliacaoRegra.naoAcionada();
        }

        String codigo = contexto.origemCobranca().getCodigo();
        if (CANAIS_SUSPEITOS.contains(codigo)) {
            return ResultadoAvaliacaoRegra.acionada(
                    "Cobrança recebida por um canal informal (" + codigo + "), mais fácil de forjar");
        }

        return ResultadoAvaliacaoRegra.naoAcionada();
    }
}
