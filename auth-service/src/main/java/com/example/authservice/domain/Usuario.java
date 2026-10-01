package com.example.authservice.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tabela USUARIO do modelo logico. Guarda o hash da senha (nunca a senha em
 * texto puro) e o status da conta (ATIVO, BLOQUEADO, etc.).
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @Column(nullable = false)
    private String status = "ATIVO";

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    @Column(name = "ultimo_login_em")
    private Instant ultimoLoginEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Instant getUltimoLoginEm() { return ultimoLoginEm; }
    public void setUltimoLoginEm(Instant ultimoLoginEm) { this.ultimoLoginEm = ultimoLoginEm; }
}
