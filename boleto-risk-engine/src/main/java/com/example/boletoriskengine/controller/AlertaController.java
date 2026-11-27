package com.example.boletoriskengine.controller;

import com.example.boletoriskengine.dto.AlertaResponse;
import com.example.boletoriskengine.service.AlertaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boleto")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // Bate exatamente com AlertaStore (httpResource) do frontend
    @GetMapping("/alertas")
    public List<AlertaResponse> listar(@RequestParam(required = false) String status) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Long usuarioId = (Long) auth.getPrincipal();
        return alertaService.listar(usuarioId, status);
    }
}
