package com.example.boletoriskengine.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Bate exatamente com BoletoAnaliseRequest do boleto.model.ts no frontend.
 */
public record BoletoAnaliseRequest(

        @NotBlank(message = "Linha digitável é obrigatória")
        String linhaDigitavel,

        @NotBlank(message = "Nome do beneficiário é obrigatório")
        String beneficiarioNome,

        @NotBlank(message = "CPF/CNPJ do beneficiário é obrigatório")
        String beneficiarioDocumento,

        @NotNull(message = "Valor informado é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valorInformado,

        @NotNull(message = "Vencimento informado é obrigatório")
        LocalDate vencimentoInformado,

        Long bancoInstituicaoId,

        Long origemCobrancaId
) {
}
