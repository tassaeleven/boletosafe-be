package com.example.boletoriskengine.controller;

import com.example.boletoriskengine.dto.AnaliseResumoResponse;
import com.example.boletoriskengine.service.AnaliseConsultaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boleto")
public class AnaliseConsultaController {

    private final AnaliseConsultaService analiseConsultaService;

    public AnaliseConsultaController(AnaliseConsultaService analiseConsultaService) {
        this.analiseConsultaService = analiseConsultaService;
    }

    // GET /api/v1/boleto/analises?linhas=...&linhas=...
    @GetMapping("/analises")
    public List<AnaliseResumoResponse> consultar(@RequestParam(name = "linhas", required = false) List<String> linhas) {
        return analiseConsultaService.consultar(linhas);
    }
}