package com.example.authservice.dto;

/**
 * Corpo devolvido por POST /api/v1/auth/register (HTTP 201).
 * Nao devolve token: depois do cadastro o usuario precisa fazer login.
 */
public record RegisterResponse(
        Long usuarioId,
        String nome,
        String email
) {
}