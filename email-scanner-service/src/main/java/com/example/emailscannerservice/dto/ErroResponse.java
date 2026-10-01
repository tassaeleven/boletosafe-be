package com.example.emailscannerservice.dto;

import java.time.Instant;

public record ErroResponse(String mensagem, Instant timestamp) {
    public ErroResponse(String mensagem) {
        this(mensagem, Instant.now());
    }
}
