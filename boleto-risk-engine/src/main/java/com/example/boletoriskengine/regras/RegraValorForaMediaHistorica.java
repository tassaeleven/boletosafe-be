package com.example.boletoriskengine.regras;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Categoria: valor e vencimento.
 * Se o beneficiario ja tem historico de cobrancas, um valor muito acima
 * da media (aqui, mais que o dobro) e um sinal de possivel adulteracao
 * do campo de valor no boleto.
 */
@Component
public class RegraValorForaMediaHistorica implements RegraDeRisco {

    private static final BigDecimal FATOR_MAXIMO = BigDecimal.valueOf(2);

    @Override
    public String codigo() {
        return "VALOR_FORA_DA_MEDIA_HISTORICA";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        BigDecimal media = contexto.mediaValorHistoricoBeneficiario();
        if (media == null || media.signum() == 0) {
            return ResultadoAvaliacaoRegra.naoAcionada();
        }

        BigDecimal limite = media.multiply(FATOR_MAXIMO);
        if (contexto.request().valorInformado().compareTo(limite) > 0) {
            return ResultadoAvaliacaoRegra.acionada(
                    "Valor informado é mais que o dobro da média histórica desse beneficiário");
        }

        return ResultadoAvaliacaoRegra.naoAcionada();
    }
}
