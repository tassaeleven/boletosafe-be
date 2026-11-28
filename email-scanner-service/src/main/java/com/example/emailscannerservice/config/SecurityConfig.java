package com.example.emailscannerservice.config;

import com.example.emailscannerservice.security.JwtAuthenticationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Esse servico precisa de DOIS mecanismos de autenticacao ao mesmo tempo:
 * - JWT do Auth Service (stateless) para as rotas normais da API
 * - Sessao OAuth2 com o Google (com estado) apenas para autorizar o
 *   acesso ao Gmail, que e inerente ao fluxo de "conectar sua conta
 *   Google" (redirect + callback do OAuth2).
 *
 * Por isso a politica de sessao aqui e IF_REQUIRED (nao STATELESS como
 * nos outros 2 microsservicos) — o login OAuth2 do Spring Security
 * precisa de sessao para completar o handshake com o Google.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public static final String GOOGLE_USUARIO_ID = "GOOGLE_USUARIO_ID";

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          OAuth2FailureHandler oAuth2FailureHandler,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    private OAuth2AuthorizationRequestResolver resolverComEscolhaDeConta() {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(c ->
                c.additionalParameters(p -> p.put("prompt", "select_account consent")));

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                guardarUsuario(request);
                return resolver.resolve(request);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
                guardarUsuario(request);
                return resolver.resolve(request, registrationId);
            }
        };
    }

    // quem clicou em "Conectar com o Google" fica registrado na sessão
    private void guardarUsuario(HttpServletRequest request) {
        String id = request.getParameter("usuarioId");
        if (id == null || !request.getRequestURI().startsWith("/oauth2/authorization")) {
            return;
        }
        try {
            request.getSession(true).setAttribute(GOOGLE_USUARIO_ID, Long.valueOf(id));
        } catch (NumberFormatException ignorado) {
            // id inválido: não registra
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // erros internos (/error) passam a aparecer como erro de verdade,
                // em vez de serem redirecionados para o login do Google
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                .anyRequest().authenticated()
            )
            // só o fluxo /oauth2 e /login pode redirecionar ao Google; qualquer
            // outra rota sem autenticação responde 401
            .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                    new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                            org.springframework.http.HttpStatus.UNAUTHORIZED),
                    request -> !request.getRequestURI().startsWith("/oauth2")
                            && !request.getRequestURI().startsWith("/login")))
            .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(a -> a.authorizationRequestResolver(resolverComEscolhaDeConta()))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)) // habilita /oauth2/authorization/google
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}