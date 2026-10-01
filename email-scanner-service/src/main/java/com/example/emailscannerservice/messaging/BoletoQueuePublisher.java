package com.example.emailscannerservice.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class BoletoQueuePublisher {

    private final RabbitTemplate rabbitTemplate;

    public BoletoQueuePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(BoletoEncontradoMessage mensagem) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_BOLETOS_ENCONTRADOS, mensagem);
    }
}
