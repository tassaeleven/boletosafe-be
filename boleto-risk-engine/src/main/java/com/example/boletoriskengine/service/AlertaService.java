package com.example.boletoriskengine.service;

import com.example.boletoriskengine.domain.Alerta;
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
        return new AlertaResponse(
                alerta.getId(),
                alerta.getAnaliseRisco().getId(),
                alerta.getTipo(),
                alerta.getDescricao(),
                alerta.getSeveridade(),
                alerta.getStatus(),
                alerta.getCriadoEm()
        );
    }
}
