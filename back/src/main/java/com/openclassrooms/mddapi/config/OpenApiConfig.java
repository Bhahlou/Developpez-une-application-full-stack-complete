package com.openclassrooms.mddapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI/Swagger documentation metadata.
 * <p>
 * Declares the JWT bearer scheme used by every protected endpoint so the
 * Swagger UI "Authorize" button can attach an access token to requests.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /**
     * @return the OpenAPI definition describing the MDD API and its JWT bearer authentication
     */
    @Bean
    OpenAPI mddApiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MDD API")
                        .description("REST API for the MDD (Monde de Dév) developer community platform.")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
