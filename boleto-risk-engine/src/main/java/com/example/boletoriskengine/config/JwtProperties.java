package com.example.boletoriskengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Mesma chave secreta do Auth Service — ver JwtProperties de lá. */
@ConfigurationProperties(prefix = "boletosafe.jwt")
public class JwtProperties {
    private String secret;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
