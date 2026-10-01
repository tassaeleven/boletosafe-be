package com.example.boletoriskengine.domain;

import jakarta.persistence.*;

/**
 * Uma linha por regra avaliada numa analise — inclusive as que NAO
 * dispararam. Isso e o que torna o score explicavel: da pra mostrar ao
 * usuario exatamente por que ele recebeu aquele resultado.
 */
@Entity
@Table(name = "resultado_regra")
public class ResultadoRegra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analise_risco_id")
    private AnaliseRisco analiseRisco;

    @ManyToOne
    @JoinColumn(name = "regra_risco_id")
    private RegraRisco regraRisco;

    private boolean acionada;

    @Column(name = "peso_aplicado")
    private int pesoAplicado;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnaliseRisco getAnaliseRisco() { return analiseRisco; }
    public void setAnaliseRisco(AnaliseRisco a) { this.analiseRisco = a; }
    public RegraRisco getRegraRisco() { return regraRisco; }
    public void setRegraRisco(RegraRisco r) { this.regraRisco = r; }
    public boolean isAcionada() { return acionada; }
    public void setAcionada(boolean acionada) { this.acionada = acionada; }
    public int getPesoAplicado() { return pesoAplicado; }
    public void setPesoAplicado(int pesoAplicado) { this.pesoAplicado = pesoAplicado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
