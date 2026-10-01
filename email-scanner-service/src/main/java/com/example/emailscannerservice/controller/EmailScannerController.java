package com.example.emailscannerservice.controller;

import com.example.emailscannerservice.dto.*;
import com.example.emailscannerservice.service.EmailScannerOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email-scanner")
public class EmailScannerController {

    private final EmailScannerOrchestrationService orchestrationService;

    public EmailScannerController(EmailScannerOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/perfil-senha")
    public void cadastrarPerfilSenha(@Valid @RequestBody CadastrarPerfilSenhaRequest request) {
        orchestrationService.cadastrarPerfilSenha(usuarioIdDoToken(), request);
    }

    @GetMapping("/boletos")
    public List<BoletoEncontradoResponse> listar() {
        return orchestrationService.listar(usuarioIdDoToken());
    }

    /**
     * SIMPLIFICAÇÃO DESTE CHECKPOINT: a varredura depende da sessão OAuth2
     * com o Google (cookie de sessão), que é um mecanismo de autenticação
     * diferente do JWT usado no resto do sistema. Por isso o usuarioId
     * ainda vem como query param aqui, em vez de ser extraído do JWT como
     * nos outros endpoints. Unificar os dois é um próximo passo — ver README.
     */
    @PostMapping("/sincronizar")
    public List<BoletoEncontradoResponse> sincronizar(OAuth2AuthenticationToken authentication,
                                                        @RequestParam Long usuarioId) throws Exception {
        return orchestrationService.sincronizar(authentication, usuarioId).stream()
                .map(orchestrationService::paraResponse)
                .toList();
    }

    @PostMapping("/boletos/{id}/desbloquear")
    public BoletoEncontradoResponse desbloquear(@PathVariable Long id,
                                                 @Valid @RequestBody DesbloquearBoletoRequest request) {
        return orchestrationService.paraResponse(
                orchestrationService.desbloquear(id, usuarioIdDoToken(), request));
    }

    private Long usuarioIdDoToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName().hashCode() * 1L : null;
    }
}
