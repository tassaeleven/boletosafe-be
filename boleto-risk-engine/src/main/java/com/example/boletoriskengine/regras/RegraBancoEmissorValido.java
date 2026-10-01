package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;

/**
 * Categoria: validacao estrutural.
 * O codigo do banco (3 primeiros digitos da linha digitavel) precisa
 * existir e estar cadastrado como uma instituicao valida em
 * BANCO_INSTITUICAO. Codigo de banco inexistente e um forte indicio de
 * boleto forjado.
 */
@Component
public class RegraBancoEmissorValido implements RegraDeRisco {

    @Override
    public String codigo() {
        return "BANCO_EMISSOR_INVALIDO";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        if (contexto.bancoInformado() == null) {
            return ResultadoAvaliacaoRegra.acionada(
                    "Código do banco emissor não corresponde a nenhuma instituição conhecida");
        }
        return ResultadoAvaliacaoRegra.naoAcionada();
    }
}
