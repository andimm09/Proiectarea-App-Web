package com.andimm09.discordclone.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * Butonul "Authorize" primeste token-ul de la /api/auth/login si il trimite la toate request-urile.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI ecouOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ecou API")
                        .version("0.0.1")
                        .description("""
                                API REST pentru Ecou: servere, canale, mesaje, invitatii.
                                1. POST /api/auth/login  2. copiezi accessToken  3. Authorize (sus, dreapta) si il lipesti."""))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
