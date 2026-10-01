package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;

/**
 * Categoria: reincidencia e historico.
 * Se este beneficiario ja gerou um alerta de severidade ALTA antes,
 * qualquer nova cobranca dele merece atencao redobrada.
 */
@Component
public class RegraReincidenciaAlertaAlto implements RegraDeRisco {

    @Override
    public String codigo() {
        return "REINCIDENCIA_ALERTA_ALTO";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        if (contexto.beneficiarioJaTeveAlertaAlto()) {
            return ResultadoAvaliacaoRegra.acionada(
                    "Este beneficiário já esteve envolvido em um alerta de risco alto anteriormente");
        }
        return ResultadoAvaliacaoRegra.naoAcionada();
    }
}
