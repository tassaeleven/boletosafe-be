package com.example.boletoriskengine.regras;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

/**
 * Categoria: beneficiario.
 * Compara o nome informado com todos os beneficiarios ja conhecidos.
 * Se for MUITO parecido (mas nao identico) a um nome existente, e indicio
 * de "typosquatting" de nome de empresa — ex.: "Fornecedor XYZ Ltda" vira
 * "Fornecedor XYZ Ltds".
 */
@Component
public class RegraNomeSimilarSuspeito implements RegraDeRisco {

    private static final LevenshteinDistance DISTANCIA = new LevenshteinDistance();
    private static final int DISTANCIA_MAXIMA_SUSPEITA = 3;

    @Override
    public String codigo() {
        return "NOME_BENEFICIARIO_SIMILAR_SUSPEITO";
    }

    @Override
    public ResultadoAvaliacaoRegra avaliar(RegraContexto contexto) {
        String nomeInformado = contexto.request().beneficiarioNome().trim().toLowerCase();

        for (var conhecido : contexto.beneficiariosConhecidos()) {
            String nomeConhecido = conhecido.getRazaoSocial().trim().toLowerCase();

            if (nomeConhecido.equals(nomeInformado)) {
                continue; // exatamente igual não é suspeito, é o mesmo beneficiário
            }

            int distancia = DISTANCIA.apply(nomeInformado, nomeConhecido);
            if (distancia > 0 && distancia <= DISTANCIA_MAXIMA_SUSPEITA) {
                return ResultadoAvaliacaoRegra.acionada(
                        "Nome do beneficiário é muito parecido com \"" + conhecido.getRazaoSocial()
                                + "\", mas não é idêntico");
            }
        }

        return ResultadoAvaliacaoRegra.naoAcionada();
    }
}
