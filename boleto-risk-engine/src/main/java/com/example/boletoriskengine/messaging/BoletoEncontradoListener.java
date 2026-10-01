package com.example.boletoriskengine.messaging;

import com.example.boletoriskengine.domain.OrigemCobranca;
import com.example.boletoriskengine.dto.BoletoAnaliseRequest;
import com.example.boletoriskengine.repository.OrigemCobrancaRepository;
import com.example.boletoriskengine.service.BoletoRiskEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BoletoEncontradoListener {

    private static final Logger log = LoggerFactory.getLogger(BoletoEncontradoListener.class);

    private final BoletoRiskEngineService boletoRiskEngineService;
    private final OrigemCobrancaRepository origemCobrancaRepository;

    public BoletoEncontradoListener(BoletoRiskEngineService boletoRiskEngineService,
                                     OrigemCobrancaRepository origemCobrancaRepository) {
        this.boletoRiskEngineService = boletoRiskEngineService;
        this.origemCobrancaRepository = origemCobrancaRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_BOLETOS_ENCONTRADOS)
    public void receber(BoletoEncontradoMessage mensagem) {
        log.info("Boleto recebido do Email Scanner Service para o usuário {}", mensagem.usuarioId());

        Long origemEmailId = origemCobrancaRepository.findByCodigo("EMAIL")
                .map(OrigemCobranca::getId)
                .orElse(null);

        BoletoAnaliseRequest request = new BoletoAnaliseRequest(
                mensagem.linhaDigitavel(),
                mensagem.beneficiarioNome(),
                mensagem.beneficiarioDocumento(),
                mensagem.valorInformado(),
                mensagem.vencimentoInformado(),
                mensagem.bancoInstituicaoId(),
                origemEmailId
        );

        // roda exatamente a mesma pipeline de regras do endpoint manual
        boletoRiskEngineService.analisar(request, mensagem.usuarioId());
    }
}
