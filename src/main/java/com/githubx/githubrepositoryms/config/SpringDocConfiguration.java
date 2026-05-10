package com.githubx.githubrepositoryms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Springdoc/Swagger UI configuration for the GitHub Repository Microservices application.
 *
 * This configuration provides global OpenAPI settings including title, description,
 * version, and security scheme definitions. The actual API endpoints are auto-discovered
 * by Springdoc from the controllers in the application's component-scanned packages.
 */
@Configuration
public class SpringDocConfiguration {

    /**
     * Define global OpenAPI metadata and security schemes.
     * This OpenAPI bean will be used by Springdoc to customize the generated API documentation.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("GitHub Repository Microservices API")
                                .description("Unified API for GitHub repository management services including branches, collaborators, content, repositories, and social features.")
                                .version("1.0.0")
                )
                .components(
                        new Components()
                                .addSecuritySchemes("smithy.api.httpBearerAuth", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .description("HTTP Bearer authentication")
                                        .scheme("bearer")
                                )
                )
        ;
    }
}

