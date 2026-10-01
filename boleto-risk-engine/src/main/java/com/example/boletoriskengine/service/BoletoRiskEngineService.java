package com.example.boletoriskengine.service;

import com.example.boletoriskengine.domain.*;
import com.example.boletoriskengine.dto.BoletoAnaliseRequest;
import com.example.boletoriskengine.dto.BoletoAnaliseResultado;
import com.example.boletoriskengine.regras.RegraContexto;
import com.example.boletoriskengine.regras.RegraDeRisco;
import com.example.boletoriskengine.regras.ResultadoAvaliacaoRegra;
import com.example.boletoriskengine.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de risco: monta o contexto, roda cada RegraDeRisco ativa cujo
 * codigo bate com uma linha em REGRA_RISCO, soma os pesos das que
 * dispararam e grava tudo (ANALISE_RISCO + RESULTADO_REGRA + ALERTA)
 * numa unica transacao.
 */
@Service
public class BoletoRiskEngineService {

    private static final Logger log = LoggerFactory.getLogger(BoletoRiskEngineService.class);

    private final BeneficiarioRepository beneficiarioRepository;
    private final BancoInstituicaoRepository bancoInstituicaoRepository;
    private final OrigemCobrancaRepository origemCobrancaRepository;
    private final BoletoRepository boletoRepository;
    private final AnaliseRiscoRepository analiseRiscoRepository;
    private final RegraRiscoRepository regraRiscoRepository;
    private final AlertaRepository alertaRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final Map<String, RegraDeRisco> regrasPorCodigo;

    public BoletoRiskEngineService(BeneficiarioRepository beneficiarioRepository,
                                    BancoInstituicaoRepository bancoInstituicaoRepository,
                                    OrigemCobrancaRepository origemCobrancaRepository,
                                    BoletoRepository boletoRepository,
                                    AnaliseRiscoRepository analiseRiscoRepository,
                                    RegraRiscoRepository regraRiscoRepository,
                                    AlertaRepository alertaRepository,
                                    LogAuditoriaRepository logAuditoriaRepository,
                                    List<RegraDeRisco> regras) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.bancoInstituicaoRepository = bancoInstituicaoRepository;
        this.origemCobrancaRepository = origemCobrancaRepository;
        this.boletoRepository = boletoRepository;
        this.analiseRiscoRepository = analiseRiscoRepository;
        this.regraRiscoRepository = regraRiscoRepository;
        this.alertaRepository = alertaRepository;
        this.logAuditoriaRepository = logAuditoriaRepository;

        // Spring injeta TODOS os beans que implementam RegraDeRisco aqui.
        // Indexamos por codigo() pra casar com a config vinda do banco.
        this.regrasPorCodigo = new HashMap<>();
        for (RegraDeRisco regra : regras) {
            regrasPorCodigo.put(regra.codigo(), regra);
        }
    }

    @Transactional
    public BoletoAnaliseResultado analisar(BoletoAnaliseRequest request, Long usuarioId) {

        BancoInstituicao banco = request.bancoInstituicaoId() != null
                ? bancoInstituicaoRepository.findById(request.bancoInstituicaoId()).orElse(null)
                : null;

        OrigemCobranca origem = request.origemCobrancaId() != null
                ? origemCobrancaRepository.findById(request.origemCobrancaId()).orElse(null)
                : null;

        String documento = request.beneficiarioDocumento().replaceAll("[^0-9]", "");
        Beneficiario beneficiarioExistente = beneficiarioRepository.findByCnpjCpf(documento).orElse(null);

        Beneficiario beneficiario = beneficiarioExistente;
        if (beneficiario == null) {
            beneficiario = new Beneficiario();
            beneficiario.setCnpjCpf(documento);
            beneficiario.setRazaoSocial(request.beneficiarioNome());
            beneficiario = beneficiarioRepository.save(beneficiario);
        }

        Boleto boleto = boletoRepository.findByLinhaDigitavel(request.linhaDigitavel())
                .orElseGet(Boleto::new);
        boleto.setLinhaDigitavel(request.linhaDigitavel());
        boleto.setValor(request.valorInformado());
        boleto.setDataVencimento(request.vencimentoInformado());
        boleto.setBancoInstituicao(banco);
        boleto = boletoRepository.save(boleto);

        BigDecimal mediaHistorica = beneficiarioExistente != null
                ? analiseRiscoRepository.mediaValorPorBeneficiario(beneficiarioExistente)
                : null;

        boolean jaTeveAlertaAlto = beneficiarioExistente != null
                && alertaRepository.existeAlertaAltoParaBeneficiario(beneficiarioExistente.getId());

        RegraContexto contexto = new RegraContexto(
                request,
                beneficiarioExistente,
                banco,
                origem,
                mediaHistorica,
                jaTeveAlertaAlto,
                beneficiarioRepository.findAll()
        );

        // ---- roda as regras ativas configuradas no banco ----
        List<RegraRisco> regrasAtivas = regraRiscoRepository.findByAtivaTrue();

        int scoreTotal = 0;
        StringBuilder observacoes = new StringBuilder();
        List<ResultadoRegra> resultados = new java.util.ArrayList<>();

        for (RegraRisco regraConfig : regrasAtivas) {
            RegraDeRisco implementacao = regrasPorCodigo.get(regraConfig.getCodigo());
            if (implementacao == null) {
                log.warn("Regra configurada '{}' não tem implementação Java correspondente", regraConfig.getCodigo());
                continue;
            }

            ResultadoAvaliacaoRegra avaliacao = implementacao.avaliar(contexto);

            ResultadoRegra resultado = new ResultadoRegra();
            resultado.setRegraRisco(regraConfig);
            resultado.setAcionada(avaliacao.acionada());

            if (avaliacao.acionada()) {
                resultado.setPesoAplicado(regraConfig.getPesoPadrao());
                resultado.setMotivo(avaliacao.motivo());
                scoreTotal += regraConfig.getPesoPadrao();
                observacoes.append("• ").append(avaliacao.motivo()).append("\n");
            }

            resultados.add(resultado);
        }

        scoreTotal = Math.min(scoreTotal, 100);
        NivelRisco nivelRisco = calcularNivel(scoreTotal);

        if (observacoes.isEmpty()) {
            observacoes.append("Nenhuma inconsistência relevante encontrada nos dados informados.");
        }

        AnaliseRisco analise = new AnaliseRisco();
        analise.setBoleto(boleto);
        analise.setBeneficiario(beneficiario);
        analise.setOrigemCobranca(origem);
        analise.setUsuarioId(usuarioId);
        analise.setValorInformado(request.valorInformado());
        analise.setVencimentoInformado(request.vencimentoInformado());
        analise.setScoreTotal(scoreTotal);
        analise.setNivelRisco(nivelRisco);
        analise.setVersaoRegrasUtilizada("v1");
        analise.setObservacoes(observacoes.toString().trim());

        resultados.forEach(r -> r.setAnaliseRisco(analise));
        analise.getResultadosRegras().addAll(resultados);

        AnaliseRisco analiseSalva = analiseRiscoRepository.save(analise);

        // beneficiario passa a "lembrar" o banco usado nesta cobranca
        beneficiario.setUltimoBancoConhecido(banco);
        beneficiarioRepository.save(beneficiario);

        criarAlertasSeNecessario(analiseSalva, resultados);
        registrarAuditoria(usuarioId, analiseSalva);

        return new BoletoAnaliseResultado(
                analiseSalva.getId(),
                analiseSalva.getScoreTotal(),
                analiseSalva.getNivelRisco().name(),
                analiseSalva.getObservacoes(),
                analiseSalva.getCriadoEm()
        );
    }

    private NivelRisco calcularNivel(int score) {
        if (score <= 30) return NivelRisco.BAIXO;
        if (score <= 70) return NivelRisco.ATENCAO;
        return NivelRisco.ALTO;
    }

    private void criarAlertasSeNecessario(AnaliseRisco analise, List<ResultadoRegra> resultados) {
        if (analise.getNivelRisco() == NivelRisco.BAIXO) {
            return;
        }

        for (ResultadoRegra resultado : resultados) {
            if (!resultado.isAcionada()) continue;

            Alerta alerta = new Alerta();
            alerta.setAnaliseRisco(analise);
            alerta.setTipo(resultado.getRegraRisco().getCodigo());
            alerta.setDescricao(resultado.getMotivo());
            alerta.setSeveridade(severidadePorPeso(resultado.getPesoAplicado()));
            alertaRepository.save(alerta);
        }
    }

    private String severidadePorPeso(int peso) {
        if (peso >= 30) return "ALTA";
        if (peso >= 15) return "MEDIA";
        return "BAIXA";
    }

    private void registrarAuditoria(Long usuarioId, AnaliseRisco analise) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuarioId(usuarioId);
        log.setAcao("ANALISE_BOLETO");
        log.setEntidadeAfetada("ANALISE_RISCO");
        log.setEntidadeId(analise.getId());
        log.setDetalhes("Score " + analise.getScoreTotal() + " / nível " + analise.getNivelRisco());
        logAuditoriaRepository.save(log);
    }
}
