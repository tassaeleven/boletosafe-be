package com.example.emailscannerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * O usuário opta explicitamente por cadastrar esses dados, cientre de que
 * serão usados só para gerar tentativas de senha de PDFs de boleto —
 * nunca armazenados em texto puro em nenhum outro lugar do sistema.
 */
public record CadastrarPerfilSenhaRequest(
        @NotBlank(message = "CPF é obrigatório") String cpf,
        @NotNull(message = "Data de nascimento é obrigatória") LocalDate dataNascimento
) {
}
