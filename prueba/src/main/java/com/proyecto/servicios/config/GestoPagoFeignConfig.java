package com.proyecto.servicios.config;

import com.proyecto.servicios.client.decoder.GestoPagoCustomErrorDecoder;
import feign.Client;
import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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

    @Bean
    public Client feignClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            sslContext.init(null, trustAllCerts, new SecureRandom());

            return new Client.Default(
                    sslContext.getSocketFactory(),
                    (hostname, session) -> true
            );
        } catch (Exception e) {
            return new Client.Default(null, null);
        }
    }
}
