package com.example.boletoriskengine.regras;

import com.example.boletoriskengine.domain.BancoInstituicao;
import com.example.boletoriskengine.domain.Beneficiario;
import com.example.boletoriskengine.domain.OrigemCobranca;
import com.example.boletoriskengine.dto.BoletoAnaliseRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Todo o "mundo" que uma regra pode precisar consultar, ja resolvido pelo
 * BoletoRiskEngineService antes de rodar as regras. Cada regra so olha
 * pra esse objeto — nao acessa repository diretamente. Isso mantem as
 * regras faceis de testar isoladas (so montar um contexto na mao).
 */
public record RegraContexto(
        BoletoAnaliseRequest request,
        Beneficiario beneficiarioExistente,   // null se for a 1a vez que aparece
        BancoInstituicao bancoInformado,      // null se o banco nao existe/nao foi enviado
        OrigemCobranca origemCobranca,        // null se nao informada
        BigDecimal mediaValorHistoricoBeneficiario, // null se nao ha historico
        boolean beneficiarioJaTeveAlertaAlto,
        List<Beneficiario> beneficiariosConhecidos
) {
    public boolean beneficiarioEhNovo() {
        return beneficiarioExistente == null;
    }
}
