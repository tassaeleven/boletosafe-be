package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Categoria: validacao estrutural.
 * O codigo do banco (3 primeiros digitos da linha digitavel) so gera alerta
 * quando NAO e de um dos principais bancos do pais. Boleto de banco incomum
 * merece mais atencao.
 */
@Component
public class RegraBancoEmissorValido implements RegraDeRisco {

    // Banco do Brasil, Santander, Caixa, Bradesco, Itau
    private static final Set<String> BANCOS_PRINCIPAIS = Set.of("001", "033", "104", "237", "341");

    @Override
    public String codigo() {
        return "BANCO_EMISSOR_INVALIDO";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        // banco informado e cadastrado (ex.: analise manual com banco escolhido)
        if (contexto.bancoInformado() != null) {
            return ResultadoAvaliacaoRegra.naoAcionada();
        }

        String linha = contexto.request().linhaDigitavel();
        String digitos = linha == null ? "" : linha.replaceAll("[^0-9]", "");
        String codigoBanco = digitos.length() >= 3 ? digitos.substring(0, 3) : "";

        if (BANCOS_PRINCIPAIS.contains(codigoBanco)) {
            return ResultadoAvaliacaoRegra.naoAcionada();
        }

        return ResultadoAvaliacaoRegra.acionada(
                "Banco emissor incomum (código " + (codigoBanco.isEmpty() ? "não identificado" : codigoBanco)
                        + "): não está entre os principais bancos");
    }
}