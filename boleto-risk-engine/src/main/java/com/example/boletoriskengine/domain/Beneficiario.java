package com.example.boletoriskengine.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "beneficiario")
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cnpj_cpf", nullable = false, unique = true)
    private String cnpjCpf;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    /** Ultimo banco emissor conhecido para esse beneficiario — usado pela
     *  regra de "mudanca de dados bancarios", o sinal mais forte do golpe
     *  do boleto falso. */
    @ManyToOne
    @JoinColumn(name = "banco_instituicao_id")
    private BancoInstituicao ultimoBancoConhecido;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCnpjCpf() { return cnpjCpf; }
    public void setCnpjCpf(String cnpjCpf) { this.cnpjCpf = cnpjCpf; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public BancoInstituicao getUltimoBancoConhecido() { return ultimoBancoConhecido; }
    public void setUltimoBancoConhecido(BancoInstituicao b) { this.ultimoBancoConhecido = b; }
    public Instant getCriadoEm() { return criadoEm; }
}
