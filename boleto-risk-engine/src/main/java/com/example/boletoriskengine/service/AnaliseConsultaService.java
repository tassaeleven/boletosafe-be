package com.example.boletoriskengine.service;

import com.example.boletoriskengine.domain.AnaliseRisco;
import com.example.boletoriskengine.dto.AnaliseResumoResponse;
import com.example.boletoriskengine.repository.AnaliseRiscoRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnaliseConsultaService {

    private final AnaliseRiscoRepository analiseRiscoRepository;

    public AnaliseConsultaService(AnaliseRiscoRepository analiseRiscoRepository) {
        this.analiseRiscoRepository = analiseRiscoRepository;
    }

    // Boleto analisado mais de uma vez: fica com a primeira análise (mesmo critério dos alertas)
    public List<AnaliseResumoResponse> consultar(List<String> linhasDigitaveis) {
        if (linhasDigitaveis == null || linhasDigitaveis.isEmpty()) {
            return List.of();
        }

        Map<String, AnaliseRisco> primeiraPorLinha = new HashMap<>();
        for (AnaliseRisco analise : analiseRiscoRepository.findByBoletoLinhaDigitavelIn(linhasDigitaveis)) {
            primeiraPorLinha.merge(analise.getBoleto().getLinhaDigitavel(), analise,
                    (a, b) -> a.getId() <= b.getId() ? a : b);
        }

        return primeiraPorLinha.values().stream()
                .map(a -> new AnaliseResumoResponse(
                        a.getBoleto().getLinhaDigitavel(),
                        a.getScoreTotal(),
                        a.getNivelRisco().name()))
                .toList();
    }
}