package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;

/**
 * Categoria: beneficiario.
 * Valida o digito verificador do CPF ou CNPJ informado. Um documento
 * com DV invalido normalmente indica dado inventado/preenchido as pressas
 * — comum em golpes montados rapidamente.
 */
@Component
public class RegraDocumentoBeneficiarioValido implements RegraDeRisco {

    @Override
    public String codigo() {
        return "DOCUMENTO_BENEFICIARIO_INVALIDO";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        String documento = contexto.request().beneficiarioDocumento().replaceAll("[^0-9]", "");

        boolean valido = switch (documento.length()) {
            case 11 -> validarCpf(documento);
            case 14 -> validarCnpj(documento);
            default -> false;
        };

        return valido
                ? ResultadoAvaliacaoRegra.naoAcionada()
                : ResultadoAvaliacaoRegra.acionada("CPF/CNPJ do beneficiário é inválido");
    }

    private boolean validarCpf(String cpf) {
        if (cpf.chars().distinct().count() == 1) return false;
        int[] pesos1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        return cpf.charAt(9) - '0' == calcularDvCpfCnpj(cpf, pesos1)
                && cpf.charAt(10) - '0' == calcularDvCpfCnpj(cpf, pesos2);
    }

    private boolean validarCnpj(String cnpj) {
        if (cnpj.chars().distinct().count() == 1) return false;
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return cnpj.charAt(12) - '0' == calcularDvCpfCnpj(cnpj, pesos1)
                && cnpj.charAt(13) - '0' == calcularDvCpfCnpj(cnpj, pesos2);
    }

    private int calcularDvCpfCnpj(String numero, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (numero.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : (11 - resto);
    }
}
