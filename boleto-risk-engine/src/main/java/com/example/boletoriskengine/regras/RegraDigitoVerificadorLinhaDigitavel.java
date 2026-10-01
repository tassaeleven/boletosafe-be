package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;

/**
 * Categoria: validacao estrutural.
 * Confere o digito verificador de cada campo da linha digitavel
 * (padrao Febraban, modulo 10). Uma linha digitavel adulterada
 * manualmente costuma quebrar esse calculo.
 */
@Component
public class RegraDigitoVerificadorLinhaDigitavel implements RegraDeRisco {

    @Override
    public String codigo() {
        return "DIGITO_VERIFICADOR_INVALIDO";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        String linha = contexto.request().linhaDigitavel().replaceAll("[^0-9]", "");

        if (linha.length() < 47) {
            return ResultadoAvaliacaoRegra.acionada(
                    "Linha digitável com tamanho inválido (esperado 47 dígitos numéricos)");
        }

        // Campos 1, 2 e 3 da linha digitavel tem 1 DV cada, calculado em modulo 10
        // sobre os 9 (ou 10) digitos anteriores do proprio campo.
        String[] campos = {
                linha.substring(0, 10),
                linha.substring(10, 21),
                linha.substring(21, 32),
        };

        for (String campo : campos) {
            String semDv = campo.substring(0, campo.length() - 1);
            char dvInformado = campo.charAt(campo.length() - 1);
            char dvCalculado = calcularDvModulo10(semDv);

            if (dvInformado != dvCalculado) {
                return ResultadoAvaliacaoRegra.acionada(
                        "Dígito verificador de um dos campos da linha digitável não confere");
            }
        }

        return ResultadoAvaliacaoRegra.naoAcionada();
    }

    private char calcularDvModulo10(String digitos) {
        int soma = 0;
        int peso = 2;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int valor = (digitos.charAt(i) - '0') * peso;
            soma += (valor > 9) ? (valor - 9) : valor;
            peso = (peso == 2) ? 1 : 2;
        }
        int resto = soma % 10;
        int dv = (resto == 0) ? 0 : (10 - resto);
        return Character.forDigit(dv, 10);
    }
}
