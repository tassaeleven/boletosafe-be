package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;

/**
 * Categoria: beneficiario.
 * Beneficiario nunca visto antes NAO e fraude por si so, mas e um fator
 * que soma risco quando combinado com outras regras (por isso tem peso
 * baixo — configurado no banco, nao aqui).
 */
@Component
public class RegraBeneficiarioNovo implements RegraDeRisco {

    @Override
    public String codigo() {
        return "BENEFICIARIO_NOVO";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        if (contexto.beneficiarioEhNovo()) {
            return ResultadoAvaliacaoRegra.acionada(
                    "Primeira cobrança registrada para este beneficiário");
        }
        return ResultadoAvaliacaoRegra.naoAcionada();
    }
}
