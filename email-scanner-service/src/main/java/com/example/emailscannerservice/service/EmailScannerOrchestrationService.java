package com.example.emailscannerservice.service;

import com.example.emailscannerservice.domain.EmailBoletoEncontrado;
import com.example.emailscannerservice.domain.PerfilSenhaUsuario;
import com.example.emailscannerservice.domain.StatusBoletoEncontrado;
import com.example.emailscannerservice.dto.CadastrarPerfilSenhaRequest;
import com.example.emailscannerservice.dto.DesbloquearBoletoRequest;
import com.example.emailscannerservice.exception.BoletoNaoEncontradoException;
import com.example.emailscannerservice.exception.SenhaIncorretaException;
import com.example.emailscannerservice.messaging.BoletoEncontradoMessage;
import com.example.emailscannerservice.messaging.BoletoQueuePublisher;
import com.example.emailscannerservice.repository.EmailBoletoEncontradoRepository;
import com.example.emailscannerservice.repository.PerfilSenhaUsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * Orquestra o fluxo completo descrito no desenho da feature:
 * busca no Gmail -> tenta extrair sem senha -> tenta padrões de senha
 * conhecidos -> se nada funcionar, marca BLOQUEADO pra desbloqueio manual.
 * Todo boleto extraído com sucesso é publicado na fila do Risk Engine.
 */
@Service
public class EmailScannerOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(EmailScannerOrchestrationService.class);

    private final GmailScannerService gmailScannerService;
    private final PdfBoletoExtractorService pdfBoletoExtractorService;
    private final SenhaPatternService senhaPatternService;
    private final BoletoQueuePublisher boletoQueuePublisher;
    private final EmailBoletoEncontradoRepository emailBoletoEncontradoRepository;
    private final PerfilSenhaUsuarioRepository perfilSenhaUsuarioRepository;

    public EmailScannerOrchestrationService(GmailScannerService gmailScannerService,
                                             PdfBoletoExtractorService pdfBoletoExtractorService,
                                             SenhaPatternService senhaPatternService,
                                             BoletoQueuePublisher boletoQueuePublisher,
                                             EmailBoletoEncontradoRepository emailBoletoEncontradoRepository,
                                             PerfilSenhaUsuarioRepository perfilSenhaUsuarioRepository) {
        this.gmailScannerService = gmailScannerService;
        this.pdfBoletoExtractorService = pdfBoletoExtractorService;
        this.senhaPatternService = senhaPatternService;
        this.boletoQueuePublisher = boletoQueuePublisher;
        this.emailBoletoEncontradoRepository = emailBoletoEncontradoRepository;
        this.perfilSenhaUsuarioRepository = perfilSenhaUsuarioRepository;
    }

    @Transactional
    public List<EmailBoletoEncontrado> sincronizar(OAuth2AuthenticationToken authentication, Long usuarioId) throws Exception {
        PerfilSenhaUsuario perfilSenha = perfilSenhaUsuarioRepository.findById(usuarioId).orElse(null);
        List<String> senhasCandidatas = senhaPatternService.gerarCandidatos(perfilSenha);

        var anexos = gmailScannerService.buscarBoletosRecentes(authentication);
        List<EmailBoletoEncontrado> processados = new java.util.ArrayList<>();

        for (var anexo : anexos) {
            if (emailBoletoEncontradoRepository.findByMensagemGmailId(anexo.mensagemId()).isPresent()) {
                continue; // já processado numa sincronização anterior
            }

            EmailBoletoEncontrado registro = new EmailBoletoEncontrado();
            registro.setUsuarioId(usuarioId);
            registro.setMensagemGmailId(anexo.mensagemId());
            registro.setRemetente(anexo.remetente());
            registro.setAssunto(anexo.assunto());
            registro.setNomeArquivoAnexo(anexo.nomeArquivo());

            var resultado = pdfBoletoExtractorService.extrair(anexo.conteudo(), senhasCandidatas);

            if (resultado.sucesso()) {
                preencherDadosExtraidos(registro, resultado);
                registro.setStatus(StatusBoletoEncontrado.ANALISADO);
                registro.setProcessadoEm(Instant.now());
                publicarParaAnalise(registro);

            } else if (resultado.precisaSenha()) {
                registro.setStatus(StatusBoletoEncontrado.BLOQUEADO);
                registro.setPdfBase64(Base64.getEncoder().encodeToString(anexo.conteudo()));

            } else {
                registro.setStatus(StatusBoletoEncontrado.ERRO_EXTRACAO);
            }

            processados.add(emailBoletoEncontradoRepository.save(registro));
        }

        return processados;
    }

    @Transactional
    public EmailBoletoEncontrado desbloquear(Long id, Long usuarioId, DesbloquearBoletoRequest request) {
        EmailBoletoEncontrado registro = emailBoletoEncontradoRepository.findById(id)
                .filter(r -> r.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new BoletoNaoEncontradoException("Boleto não encontrado"));

        byte[] pdfBytes = Base64.getDecoder().decode(registro.getPdfBase64());
        var resultado = pdfBoletoExtractorService.extrairComSenhaManual(pdfBytes, request.senha());

        if (!resultado.sucesso()) {
            throw new SenhaIncorretaException("Senha incorreta ou PDF não pôde ser lido");
        }

        preencherDadosExtraidos(registro, resultado);
        registro.setStatus(StatusBoletoEncontrado.ANALISADO);
        registro.setProcessadoEm(Instant.now());
        registro.setPdfBase64(null); // não guarda mais o PDF depois de desbloqueado
        publicarParaAnalise(registro);

        return emailBoletoEncontradoRepository.save(registro);
    }

    public List<com.example.emailscannerservice.dto.BoletoEncontradoResponse> listar(Long usuarioId) {
        return emailBoletoEncontradoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId).stream()
                .map(this::paraResponse)
                .toList();
    }

    public com.example.emailscannerservice.dto.BoletoEncontradoResponse paraResponse(EmailBoletoEncontrado r) {
        return new com.example.emailscannerservice.dto.BoletoEncontradoResponse(
                r.getId(),
                r.getRemetente(),
                r.getAssunto(),
                r.getNomeArquivoAnexo(),
                r.getStatus().name(),
                r.getBeneficiarioNomeExtraido(),
                r.getValorExtraido(),
                r.getVencimentoExtraido(),
                r.getCriadoEm()
        );
    }

    public void cadastrarPerfilSenha(Long usuarioId, CadastrarPerfilSenhaRequest request) {
        PerfilSenhaUsuario perfil = perfilSenhaUsuarioRepository.findById(usuarioId).orElseGet(PerfilSenhaUsuario::new);
        perfil.setUsuarioId(usuarioId);
        perfil.setCpf(request.cpf().replaceAll("[^0-9]", ""));
        perfil.setDataNascimento(request.dataNascimento());
        perfilSenhaUsuarioRepository.save(perfil);
    }

    private void preencherDadosExtraidos(EmailBoletoEncontrado registro, PdfBoletoExtractorService.ResultadoExtracao resultado) {
        registro.setLinhaDigitavelExtraida(resultado.linhaDigitavel());
        registro.setBeneficiarioNomeExtraido(resultado.beneficiarioNome());
        registro.setBeneficiarioDocumentoExtraido(resultado.beneficiarioDocumento());
        registro.setValorExtraido(resultado.valor());
        registro.setVencimentoExtraido(resultado.vencimento());
    }

    private void publicarParaAnalise(EmailBoletoEncontrado registro) {
        boletoQueuePublisher.publicar(new BoletoEncontradoMessage(
                registro.getUsuarioId(),
                registro.getLinhaDigitavelExtraida(),
                registro.getBeneficiarioNomeExtraido(),
                registro.getBeneficiarioDocumentoExtraido(),
                registro.getValorExtraido(),
                registro.getVencimentoExtraido(),
                null // banco emissor: o Risk Engine tenta resolver pela linha digitável
        ));
        log.info("Boleto {} publicado na fila para análise de risco", registro.getId());
    }
}
