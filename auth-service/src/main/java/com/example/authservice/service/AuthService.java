package com.example.authservice.service;

import com.example.authservice.domain.Perfil;
import com.example.authservice.domain.Usuario;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.RegisterResponse;
import com.example.authservice.exception.CredenciaisInvalidasException;
import com.example.authservice.exception.EmailJaCadastradoException;
import com.example.authservice.repository.PerfilRepository;
import com.example.authservice.repository.UsuarioRepository;
import com.example.authservice.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {

    private static final String PERFIL_PADRAO = "OPERADOR";

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                        PerfilRepository perfilRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(request.email()))
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

    /**
     * Cadastra um novo usuario com o perfil padrao. Nao gera token: o
     * frontend manda o usuario para a tela de login depois do cadastro.
     */
    @Transactional
    public RegisterResponse registrar(RegisterRequest request) {
        String email = normalizar(request.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException("Já existe uma conta com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(obterPerfilPadrao());

        try {
            // saveAndFlush forca o INSERT agora, pra capturar corrida de dois
            // cadastros simultaneos com o mesmo e-mail (unique no banco)
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailJaCadastradoException("Já existe uma conta com este e-mail");
        }

        return new RegisterResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    private Perfil obterPerfilPadrao() {
        return perfilRepository.findByCodigo(PERFIL_PADRAO).orElseGet(() -> {
            Perfil perfil = new Perfil();
            perfil.setCodigo(PERFIL_PADRAO);
            perfil.setNome("Operador");
            perfil.setDescricao("Usuário que analisa boletos no dia a dia");
            return perfilRepository.save(perfil);
        });
    }

    // E-mail e guardado e buscado sempre em minusculas, senao
    // "Joao@x.com" e "joao@x.com" virariam duas contas diferentes.
    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}