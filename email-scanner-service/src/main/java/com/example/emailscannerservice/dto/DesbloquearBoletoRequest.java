package com.example.emailscannerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record DesbloquearBoletoRequest(
        @NotBlank(message = "Senha é obrigatória") String senha
) {
}
