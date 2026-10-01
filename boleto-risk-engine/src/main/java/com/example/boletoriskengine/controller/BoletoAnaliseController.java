package com.example.boletoriskengine.controller;

import com.example.boletoriskengine.dto.BoletoAnaliseRequest;
import com.example.boletoriskengine.dto.BoletoAnaliseResultado;
import com.example.boletoriskengine.service.BoletoRiskEngineService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boleto")
public class BoletoAnaliseController {

    private final BoletoRiskEngineService boletoRiskEngineService;

    public BoletoAnaliseController(BoletoRiskEngineService boletoRiskEngineService) {
        this.boletoRiskEngineService = boletoRiskEngineService;
    }

    // Bate exatamente com BoletoService.ts do frontend (POST /api/v1/boleto/analise)
    @PostMapping("/analise")
    public BoletoAnaliseResultado analisar(@Valid @RequestBody BoletoAnaliseRequest request) {
        Long usuarioId = extrairUsuarioIdDoToken();
        return boletoRiskEngineService.analisar(request, usuarioId);
    }

    private Long extrairUsuarioIdDoToken() {
        // O JwtAuthenticationFilter coloca o e-mail como "name" da autenticação;
        // usuarioId em si é opcional aqui (so para auditoria) — simplificado
        // para não obrigar uma chamada extra ao Auth Service.
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName().hashCode() * 1L : null;
    }
}
