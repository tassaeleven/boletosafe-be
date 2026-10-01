package com.example.emailscannerservice.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Dados que o PRÓPRIO usuário cadastra, por opção, pra permitir o "chute
 * inteligente" de senha de PDF (padrões bancários: primeiros dígitos do
 * CPF, data de nascimento etc.). Sem isso cadastrado, todo PDF criptografado
 * cai direto como BLOQUEADO, pedindo senha manual.
 */
@Entity
@Table(name = "perfil_senha_usuario")
public class PerfilSenhaUsuario {

    @Id
    private Long usuarioId; // mesmo ID do USUARIO no Auth Service

    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
}
