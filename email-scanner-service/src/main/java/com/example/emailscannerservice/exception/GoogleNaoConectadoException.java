package com.example.emailscannerservice.exception;

public class GoogleNaoConectadoException extends RuntimeException {
    public GoogleNaoConectadoException(String mensagem) {
        super(mensagem);
    }
}