package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;

/**
 * Categoria: beneficiario.
 * ESTE E O SINAL MAIS FORTE do golpe do boleto falso: um beneficiario ja
 * conhecido, cujo banco emissor mudou desde a ultima cobranca recebida
 * dele. E exatamente o padrao de um golpista interceptar o boleto e trocar
 * os dados bancarios mantendo o mesmo nome/documento do fornecedor real.
 */
@Component
public class RegraMudancaDadosBancarios implements RegraDeRisco {

    @Override
    public String codigo() {
        return "MUDANCA_DADOS_BANCARIOS";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        if (contexto.beneficiarioEhNovo() || contexto.bancoInformado() == null) {
            return ResultadoAvaliacaoRegra.naoAcionada();
        }

        var bancoConhecido = contexto.beneficiarioExistente().getUltimoBancoConhecido();
        if (bancoConhecido == null) {
            return ResultadoAvaliacaoRegra.naoAcionada();
        }

        boolean mudou = !bancoConhecido.getId().equals(contexto.bancoInformado().getId());

        return mudou
                ? ResultadoAvaliacaoRegra.acionada(
                        "O banco emissor deste boleto é diferente do último banco usado por este beneficiário")
                : ResultadoAvaliacaoRegra.naoAcionada();
    }
}
