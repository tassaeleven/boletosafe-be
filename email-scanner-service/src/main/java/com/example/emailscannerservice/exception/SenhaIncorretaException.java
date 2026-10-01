package com.example.emailscannerservice.exception;

public class SenhaIncorretaException extends RuntimeException {
    public SenhaIncorretaException(String mensagem) { super(mensagem); }
}
