package com.example.authservice.service;

import com.example.authservice.domain.Usuario;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.exception.CredenciaisInvalidasException;
import com.example.authservice.repository.UsuarioRepository;
import com.example.authservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos"));

        if (!"ATIVO".equals(usuario.getStatus())) {
            throw new CredenciaisInvalidasException("Usuário inativo ou bloqueado");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos");
        }

        usuario.setUltimoLoginEm(Instant.now());
        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(token, usuario.getId(), usuario.getNome(), usuario.getPerfil().getCodigo());
    }
}
