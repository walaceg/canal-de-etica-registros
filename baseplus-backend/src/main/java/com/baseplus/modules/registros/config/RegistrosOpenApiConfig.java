package com.baseplus.modules.registros.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springdoc.core.models.GroupedOpenApi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class RegistrosOpenApiConfig {

    @Bean
    public GroupedOpenApi canalDeEticaOpenApiGroup() {
        return GroupedOpenApi.builder()
                .group("Canal de Ética")
                .pathsToMatch(
                        "/api/public/v1/registros",
                        "/api/registros",
                        "/api/registros/{id}"
                )
                .build();
    }

    @Bean
    public OpenAPI registrosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Canal de Etica Registros API")
                        .version("1.0.2")
                        .description("APIs publica e interna para registros do Canal de Etica."))
                .components(new Components()
                        .addSecuritySchemes("API Key", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key"))
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
