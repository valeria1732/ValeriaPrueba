package com.proyecto.servicios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApi {

    @Value("${server.port:8088}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Servicios Empresa")
                        .version("1.0")
                        .description("Documentación interactiva de microservicios: Clientes, Cuentas Bancarias y Productos"))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Servidor Local")
                ));
    }
}
