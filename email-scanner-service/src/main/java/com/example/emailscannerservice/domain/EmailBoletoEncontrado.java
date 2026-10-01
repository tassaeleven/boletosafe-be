package com.example.emailscannerservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Um boleto (ou tentativa de boleto) encontrado durante a varredura do
 * e-mail de um usuário. Guarda o que foi extraído (quando deu certo) e o
 * PDF original em base64 (quando ainda está BLOQUEADO), pra permitir que o
 * usuário desbloqueie depois informando a senha manualmente.
 */
@Entity
@Table(name = "email_boleto_encontrado")
public class EmailBoletoEncontrado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "mensagem_gmail_id", nullable = false, unique = true)
    private String mensagemGmailId;

    private String remetente;

    private String assunto;

    @Column(name = "nome_arquivo_anexo")
    private String nomeArquivoAnexo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBoletoEncontrado status;

    // preenchidos so quando a extracao da certo
    @Column(name = "linha_digitavel_extraida")
    private String linhaDigitavelExtraida;

    @Column(name = "beneficiario_nome_extraido")
    private String beneficiarioNomeExtraido;

    @Column(name = "beneficiario_documento_extraido")
    private String beneficiarioDocumentoExtraido;

    @Column(name = "valor_extraido")
    private BigDecimal valorExtraido;

    @Column(name = "vencimento_extraido")
    private LocalDate vencimentoExtraido;

    // guardado so enquanto BLOQUEADO, pra permitir desbloqueio manual depois
    @Lob
    @Column(name = "pdf_base64")
    private String pdfBase64;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "processado_em")
    private Instant processadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getMensagemGmailId() { return mensagemGmailId; }
    public void setMensagemGmailId(String m) { this.mensagemGmailId = m; }
    public String getRemetente() { return remetente; }
    public void setRemetente(String remetente) { this.remetente = remetente; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public String getNomeArquivoAnexo() { return nomeArquivoAnexo; }
    public void setNomeArquivoAnexo(String n) { this.nomeArquivoAnexo = n; }
    public StatusBoletoEncontrado getStatus() { return status; }
    public void setStatus(StatusBoletoEncontrado status) { this.status = status; }
    public String getLinhaDigitavelExtraida() { return linhaDigitavelExtraida; }
    public void setLinhaDigitavelExtraida(String l) { this.linhaDigitavelExtraida = l; }
    public String getBeneficiarioNomeExtraido() { return beneficiarioNomeExtraido; }
    public void setBeneficiarioNomeExtraido(String b) { this.beneficiarioNomeExtraido = b; }
    public String getBeneficiarioDocumentoExtraido() { return beneficiarioDocumentoExtraido; }
    public void setBeneficiarioDocumentoExtraido(String b) { this.beneficiarioDocumentoExtraido = b; }
    public BigDecimal getValorExtraido() { return valorExtraido; }
    public void setValorExtraido(BigDecimal v) { this.valorExtraido = v; }
    public LocalDate getVencimentoExtraido() { return vencimentoExtraido; }
    public void setVencimentoExtraido(LocalDate v) { this.vencimentoExtraido = v; }
    public String getPdfBase64() { return pdfBase64; }
    public void setPdfBase64(String pdfBase64) { this.pdfBase64 = pdfBase64; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getProcessadoEm() { return processadoEm; }
    public void setProcessadoEm(Instant processadoEm) { this.processadoEm = processadoEm; }
}
