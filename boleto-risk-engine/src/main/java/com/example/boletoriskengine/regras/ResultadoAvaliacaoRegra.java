package com.example.boletoriskengine.regras;

public record ResultadoAvaliacaoRegra(boolean acionada, String motivo) {

    public static ResultadoAvaliacaoRegra naoAcionada() {
        return new ResultadoAvaliacaoRegra(false, null);
    }

    public static ResultadoAvaliacaoRegra acionada(String motivo) {
        return new ResultadoAvaliacaoRegra(true, motivo);
    }
}
