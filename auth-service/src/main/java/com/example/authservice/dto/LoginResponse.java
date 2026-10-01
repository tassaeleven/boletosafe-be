package com.example.authservice.dto;

/**
 * Corpo devolvido por POST /api/v1/auth/login.
 * Bate exatamente com o LoginResponse esperado pelo AuthService.ts do frontend.
 */
public record LoginResponse(
        String token,
        Long usuarioId,
        String nome,
        String perfil
) {
}
