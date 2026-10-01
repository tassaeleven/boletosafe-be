package com.example.boletoriskengine.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "log_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String acao;

    @Column(name = "entidade_afetada")
    private String entidadeAfetada;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    public String getEntidadeAfetada() { return entidadeAfetada; }
    public void setEntidadeAfetada(String e) { this.entidadeAfetada = e; }
    public Long getEntidadeId() { return entidadeId; }
    public void setEntidadeId(Long entidadeId) { this.entidadeId = entidadeId; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public Instant getCriadoEm() { return criadoEm; }
}
