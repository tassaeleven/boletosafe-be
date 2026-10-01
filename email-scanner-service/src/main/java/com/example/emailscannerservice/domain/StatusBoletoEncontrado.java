package com.example.emailscannerservice.domain;

public enum StatusBoletoEncontrado {
    /** PDF veio criptografado e nenhuma senha (nem os padrões conhecidos) funcionou. */
    BLOQUEADO,
    /** Dados extraídos com sucesso e já enviados pra fila do Risk Engine. */
    ANALISADO,
    /** Algo deu errado ao tentar ler o PDF (arquivo corrompido, layout não reconhecido etc.). */
    ERRO_EXTRACAO
}
