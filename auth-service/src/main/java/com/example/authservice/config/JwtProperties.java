package com.example.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mapeia o prefixo "boletosafe.jwt" do application.yml.
 * O mesmo "secret" precisa estar configurado nos 3 microsservicos,
 * pois e o que permite que o Risk Engine e o Email Scanner validem
 * um token emitido aqui, sem chamar o Auth Service a cada requisicao.
 */
@ConfigurationProperties(prefix = "boletosafe.jwt")
public class JwtProperties {

    /** Chave secreta HMAC (>= 32 caracteres) compartilhada entre os servicos. */
    private String secret;

    /** Tempo de expiracao do token, em minutos. */
    private long expiracaoMinutos = 60;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpiracaoMinutos() { return expiracaoMinutos; }
    public void setExpiracaoMinutos(long expiracaoMinutos) { this.expiracaoMinutos = expiracaoMinutos; }
}
