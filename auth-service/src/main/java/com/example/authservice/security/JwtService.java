package com.example.authservice.security;

import com.example.authservice.config.JwtProperties;
import com.example.authservice.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Gera e valida o JWT. A mesma logica de validacao (metodo extrairClaims)
 * e reproduzida como filtro nos outros 2 microsservicos, usando o mesmo
 * "secret" — e assim que eles confiam num token emitido aqui sem se
 * comunicarem a cada requisicao.
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey chave;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.chave = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(properties.getExpiracaoMinutos(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("usuarioId", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getPerfil().getCodigo())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave)
                .compact();
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
