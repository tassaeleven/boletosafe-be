package com.example.boletoriskengine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "boleto")
public class Boleto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "linha_digitavel", nullable = false, unique = true)
    private String linhaDigitavel;

    @Column(name = "codigo_barras")
    private String codigoBarras;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @ManyToOne
    @JoinColumn(name = "banco_instituicao_id")
    private BancoInstituicao bancoInstituicao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLinhaDigitavel() { return linhaDigitavel; }
    public void setLinhaDigitavel(String linhaDigitavel) { this.linhaDigitavel = linhaDigitavel; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public BancoInstituicao getBancoInstituicao() { return bancoInstituicao; }
    public void setBancoInstituicao(BancoInstituicao b) { this.bancoInstituicao = b; }
    public Instant getCriadoEm() { return criadoEm; }
}
