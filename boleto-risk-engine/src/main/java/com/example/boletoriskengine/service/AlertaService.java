package com.example.boletoriskengine.service;

import com.example.boletoriskengine.domain.Alerta;
import com.example.boletoriskengine.domain.AnaliseRisco;
import com.example.boletoriskengine.domain.Beneficiario;
import com.example.boletoriskengine.domain.Boleto;
import com.example.boletoriskengine.dto.AlertaResponse;
import com.example.boletoriskengine.repository.AlertaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

        return alertas.stream().map(this::paraResponse).toList();
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
                analise.getNivelRisco().name()
        );
    }
}