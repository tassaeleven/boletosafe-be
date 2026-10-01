package com.example.boletoriskengine.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Configuracao de cada regra (nao o codigo dela). O codigo Java em
 * regras/*.java implementa a logica; esta tabela guarda o PESO e se a
 * regra esta ATIVA — assim da pra ligar/desligar ou recalibrar peso sem
 * fazer deploy.
 */
@Entity
@Table(name = "regra_risco")
public class RegraRisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Precisa bater com o codigo() da implementacao em regras/*.java. */
    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String categoria;

    @Column(name = "peso_padrao", nullable = false)
    private int pesoPadrao;

    private int versao = 1;

    private boolean ativa = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getPesoPadrao() { return pesoPadrao; }
    public void setPesoPadrao(int pesoPadrao) { this.pesoPadrao = pesoPadrao; }
    public int getVersao() { return versao; }
    public void setVersao(int versao) { this.versao = versao; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public Instant getCriadoEm() { return criadoEm; }
}
