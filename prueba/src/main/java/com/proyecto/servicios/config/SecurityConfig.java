package com.proyecto.servicios.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Configuration
public class SecurityConfig {

    private static final String EPHEMERAL_FALLBACK_SECRET = generarSecretoEfimero();

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    public String generarToken(String correo, Integer clienteId, Integer usuarioId) {
        return JWT.create()
                .withSubject(correo)
                .withClaim("clienteId", clienteId)
                .withClaim("usuarioId", usuarioId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign( obtenerAlgoritmo());
    }

    public DecodedJWT validarYDecodificarToken(String token) {
        return JWT.require(obtenerAlgoritmo()).build().verify(token);
    }

    private Algorithm obtenerAlgoritmo() {
        String secret = StringUtils.isNotBlank(jwtSecret) ? jwtSecret : EPHEMERAL_FALLBACK_SECRET;
        return Algorithm.HMAC256(secret);
    }

    private static String generarSecretoEfimero() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
