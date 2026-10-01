package com.example.emailscannerservice.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MESMO nome de fila usado no RabbitMQConfig do boleto-risk-engine —
 * é assim que os dois microsserviços se enxergam via RabbitMQ, sem
 * chamada HTTP direta entre eles.
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
