package com.example.emailscannerservice.security;

import com.example.emailscannerservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/** So VALIDA tokens emitidos pelo Auth Service — mesma chave secreta. */
@Service
public class JwtService {

    private final SecretKey chave;

    public JwtService(JwtProperties properties) {
        this.chave = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
    }
}
