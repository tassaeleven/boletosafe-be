package com.example.emailscannerservice.exception;

import com.example.emailscannerservice.dto.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoletoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(BoletoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(SenhaIncorretaException.class)
    public ResponseEntity<ErroResponse> tratarSenhaIncorreta(SenhaIncorretaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Dados inválidos");
        return ResponseEntity.badRequest().body(new ErroResponse(mensagem));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroGenerico(Exception ex) {
        return ResponseEntity.internalServerError().body(new ErroResponse("Erro interno no Email Scanner Service"));
    }
}
