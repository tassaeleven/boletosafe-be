package com.example.boletoriskengine.service;

import com.example.boletoriskengine.domain.Alerta;
import com.example.boletoriskengine.domain.AnaliseRisco;
import com.example.boletoriskengine.domain.Beneficiario;
import com.example.boletoriskengine.domain.Boleto;
import com.example.boletoriskengine.dto.AlertaResponse;
import com.example.boletoriskengine.repository.AlertaRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    // GET /api/v1/boleto/alertas — usado pelo AlertaStore (httpResource) do frontend
    public List<AlertaResponse> listar(String status) {
        List<Alerta> alertas = (status != null && !status.isBlank())
                ? alertaRepository.findByStatus(status)
                : alertaRepository.findAll();

        List<AlertaResponse> todas = alertas.stream().map(this::paraResponse).toList();

        // boleto idêntico analisado mais de uma vez: mantém só a primeira análise
        Map<String, Long> primeiraAnalise = new HashMap<>();
        for (AlertaResponse a : todas) {
            primeiraAnalise.merge(chaveDoBoleto(a), a.analiseRiscoId(), Math::min);
        }

        return todas.stream()
                .filter(a -> a.analiseRiscoId().equals(primeiraAnalise.get(chaveDoBoleto(a))))
                .toList();
    }

    private String chaveDoBoleto(AlertaResponse a) {
        return a.linhaDigitavel() + "|" + a.valor().stripTrailingZeros().toPlainString()
                + "|" + a.vencimento();
    }

    private AlertaResponse paraResponse(Alerta alerta) {
        AnaliseRisco analise = alerta.getAnaliseRisco();
        Boleto boleto = analise.getBoleto();
        Beneficiario beneficiario = analise.getBeneficiario();

        return new AlertaResponse(
                alerta.getId(),
                analise.getId(),
                alerta.getTipo(),
                alerta.getDescricao(),
                alerta.getSeveridade(),
                alerta.getStatus(),
                alerta.getCriadoEm(),
                boleto != null ? boleto.getLinhaDigitavel() : null,
                beneficiario != null ? beneficiario.getRazaoSocial() : null,
                beneficiario != null ? beneficiario.getCnpjCpf() : null,
                analise.getValorInformado(),
                analise.getVencimentoInformado(),
                analise.getScoreTotal(),
                analise.getNivelRisco().name(),
                analise.getTitulo()
        );
    }
}