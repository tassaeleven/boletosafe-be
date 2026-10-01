package com.example.boletoriskengine.domain;

import jakarta.persistence.*;

/** Ex.: EMAIL, WHATSAPP, SMS, PORTAL, CARTA. */
@Entity
@Table(name = "origem_cobranca")
public class OrigemCobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String descricao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
