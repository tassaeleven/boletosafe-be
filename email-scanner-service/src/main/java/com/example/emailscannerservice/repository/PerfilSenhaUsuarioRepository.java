package com.example.emailscannerservice.repository;

import com.example.emailscannerservice.domain.PerfilSenhaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilSenhaUsuarioRepository extends JpaRepository<PerfilSenhaUsuario, Long> {
}
