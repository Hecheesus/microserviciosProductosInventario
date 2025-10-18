package com.microservices.inventario.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Inventario",
                version = "1.0.0",
                description = "Microservicio para gestión de inventarios - JSON API compliant"
        ),
        servers = {
                @Server(url = "http://localhost:8082", description = "Servidor de Desarrollo"),
                @Server(url = "http://inventario-service:8082", description = "Servidor Docker")
        },
        security = @SecurityRequirement(name = "apiKey")

)
@SecurityScheme(
        name = "apiKey",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "API Key para autenticación (usar: inventario-api-key-2024)"
)
public class OpenApiConfig {
}
