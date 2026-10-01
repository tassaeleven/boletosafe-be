package com.example.boletoriskengine.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "banco_instituicao")
public class BancoInstituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_febraban", nullable = false, unique = true)
    private String codigoFebraban;

    @Column(nullable = false)
    private String nome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoFebraban() { return codigoFebraban; }
    public void setCodigoFebraban(String codigoFebraban) { this.codigoFebraban = codigoFebraban; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
