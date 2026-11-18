package com.example.emailscannerservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Depois que o Google autoriza o acesso, devolve o usuário para o
 * frontend Angular (em vez de cair numa página do próprio serviço).
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final String frontendUrl;

    public OAuth2SuccessHandler(@Value("${boletosafe.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.sendRedirect(frontendUrl + "/email-scanner?google=conectado");
    }
}
