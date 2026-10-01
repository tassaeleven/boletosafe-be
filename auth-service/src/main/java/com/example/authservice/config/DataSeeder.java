package com.example.authservice.config;

import com.example.authservice.domain.Perfil;
import com.example.authservice.domain.Usuario;
import com.example.authservice.repository.PerfilRepository;
import com.example.authservice.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * So roda em ambiente de desenvolvimento (perfil "dev"), pra ja existir um
 * usuario de teste sem precisar rodar INSERT manual no banco.
 *
 * Login de teste: teste@boletosafe.com / 123456
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(PerfilRepository perfilRepository,
                       UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Perfil operador = new Perfil();
        operador.setCodigo("OPERADOR");
        operador.setNome("Operador");
        operador.setDescricao("Usuário que analisa boletos no dia a dia");
        perfilRepository.save(operador);

        Usuario usuario = new Usuario();
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@boletosafe.com");
        usuario.setSenhaHash(passwordEncoder.encode("123456"));
        usuario.setPerfil(operador);
        usuarioRepository.save(usuario);
    }
}
