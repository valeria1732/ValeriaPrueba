package com.proyecto.servicios.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret:SuperSecretJwtSecretKeyForFinancialOnboarding2026!}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}") // 24 horas por defecto
    private long jwtExpirationMs;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public String generarToken(String correo, Integer clienteId, Integer usuarioId) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
                .withSubject(correo)
                .withClaim("clienteId", clienteId)
                .withClaim("usuarioId", usuarioId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(algorithm);
    }

    public DecodedJWT validarYDecodificarToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.require(algorithm).build().verify(token);
    }
}
