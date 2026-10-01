package com.example.boletoriskengine.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analise_risco_id")
    private AnaliseRisco analiseRisco;

    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    /** BAIXA, MEDIA, ALTA */
    private String severidade;

    /** ABERTO, VISUALIZADO, RESOLVIDO */
    private String status = "ABERTO";

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "visualizado_em")
    private Instant visualizadoEm;

    @Column(name = "resolvido_em")
    private Instant resolvidoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnaliseRisco getAnaliseRisco() { return analiseRisco; }
    public void setAnaliseRisco(AnaliseRisco a) { this.analiseRisco = a; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getSeveridade() { return severidade; }
    public void setSeveridade(String severidade) { this.severidade = severidade; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getVisualizadoEm() { return visualizadoEm; }
    public void setVisualizadoEm(Instant v) { this.visualizadoEm = v; }
    public Instant getResolvidoEm() { return resolvidoEm; }
    public void setResolvidoEm(Instant r) { this.resolvidoEm = r; }
}
