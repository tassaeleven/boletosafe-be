package com.example.boletoriskengine.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            responderNaoAutorizado(response, "Token ausente");
            return;
        }

        Claims claims;
        try {
            claims = jwtService.extrairClaims(authHeader.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token recusado: " + e.getMessage());
            responderNaoAutorizado(response, "Token inválido ou expirado");
            return;
        }

        String perfil = claims.get("perfil", String.class);
        var authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    // JSON escrito à mão: evita depender do módulo de datas do Jackson aqui
    private void responderNaoAutorizado(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"mensagem\":\"" + mensagem + "\"}");
    }
}