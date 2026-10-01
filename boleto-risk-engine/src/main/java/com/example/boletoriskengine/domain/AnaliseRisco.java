package com.example.boletoriskengine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro central de uma analise: guarda os DADOS INFORMADOS pelo usuario
 * (que podem nao bater com o Boleto ja conhecido) e o resultado calculado.
 * "versaoRegrasUtilizada" existe para auditoria: dado um score antigo,
 * da pra saber exatamente qual conjunto de regras o gerou.
 */
@Entity
@Table(name = "analise_risco")
public class AnaliseRisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "boleto_id")
    private Boleto boleto;

    @ManyToOne
    @JoinColumn(name = "beneficiario_id")
    private Beneficiario beneficiario;

    @ManyToOne
    @JoinColumn(name = "origem_cobranca_id")
    private OrigemCobranca origemCobranca;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "valor_informado", nullable = false)
    private BigDecimal valorInformado;

    @Column(name = "vencimento_informado", nullable = false)
    private LocalDate vencimentoInformado;

    @Column(name = "score_total", nullable = false)
    private int scoreTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", nullable = false)
    private NivelRisco nivelRisco;

    @Column(name = "versao_regras_utilizada")
    private String versaoRegrasUtilizada;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @OneToMany(mappedBy = "analiseRisco", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoRegra> resultadosRegras = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Boleto getBoleto() { return boleto; }
    public void setBoleto(Boleto boleto) { this.boleto = boleto; }
    public Beneficiario getBeneficiario() { return beneficiario; }
    public void setBeneficiario(Beneficiario beneficiario) { this.beneficiario = beneficiario; }
    public OrigemCobranca getOrigemCobranca() { return origemCobranca; }
    public void setOrigemCobranca(OrigemCobranca o) { this.origemCobranca = o; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public BigDecimal getValorInformado() { return valorInformado; }
    public void setValorInformado(BigDecimal v) { this.valorInformado = v; }
    public LocalDate getVencimentoInformado() { return vencimentoInformado; }
    public void setVencimentoInformado(LocalDate v) { this.vencimentoInformado = v; }
    public int getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(int scoreTotal) { this.scoreTotal = scoreTotal; }
    public NivelRisco getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(NivelRisco nivelRisco) { this.nivelRisco = nivelRisco; }
    public String getVersaoRegrasUtilizada() { return versaoRegrasUtilizada; }
    public void setVersaoRegrasUtilizada(String v) { this.versaoRegrasUtilizada = v; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public Instant getCriadoEm() { return criadoEm; }
    public List<ResultadoRegra> getResultadosRegras() { return resultadosRegras; }
}
