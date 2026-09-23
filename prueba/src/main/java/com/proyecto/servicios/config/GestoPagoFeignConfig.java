package com.proyecto.servicios.config;

import com.proyecto.servicios.client.decoder.GestoPagoCustomErrorDecoder;
import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GestoPagoFeignConfig {

    @Value("${feign.client.config.gestoPagoProductClient.connectTimeout:5000}")
    private int connectTimeoutMs;

    @Value("${feign.client.config.gestoPagoProductClient.readTimeout:10000}")
    private int readTimeoutMs;

    @Bean
    public ErrorDecoder errorDecoder() {
        return new GestoPagoCustomErrorDecoder();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                connectTimeoutMs,
                TimeUnit.MILLISECONDS,
                readTimeoutMs,
                TimeUnit.MILLISECONDS,
                true
        );
    }
}
