package com.example.boletoriskengine.regras;

/**
 * Contrato que toda regra de risco implementa. O "codigo()" precisa bater
 * com o campo REGRA_RISCO.codigo no banco — e assim que o motor liga o
 * peso configurado no banco com a implementacao Java correspondente.
 */
public interface RegraDeRisco {

    String codigo();

    ResultadoAvaliacaoRegra avaliar(RegraContexto contexto);
}
