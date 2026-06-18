package com.example.ms_order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Microservicio de Órdenes (ms-order)")
                        .version("1.0.0")
                        .description("Documentación de los endpoints para la gestión de órdenes de compra, ítems y vinculación con productos usando HATEOAS.")
                        .contact(new Contact()
                                .name("Soporte Desarrollo")
                                .email("desarrollo@example.com")));
    }
}