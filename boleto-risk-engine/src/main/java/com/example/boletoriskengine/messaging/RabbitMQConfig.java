package com.example.boletoriskengine.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fila pela qual o Email Scanner Service publica boletos encontrados no
 * e-mail do usuario. O Risk Engine consome e roda a MESMA analise de
 * regras usada no fluxo manual (BoletoRiskEngineService.analisar) —
 * so muda a origem do dado.
 */
@Configuration
public class RabbitMQConfig {

    public static final String FILA_BOLETOS_ENCONTRADOS = "boletosafe.email-scanner.boletos-encontrados";

    @Bean
    public Queue filaBoletosEncontrados() {
        return new Queue(FILA_BOLETOS_ENCONTRADOS, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
