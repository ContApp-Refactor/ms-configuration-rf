package co.unicauca.edu.co.contables.auxiliary.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * @brief Configuración de Swagger/OpenAPI para la API de Libros Auxiliares.
 *
 * Registra el esquema de seguridad "bearer-jwt" y publica la información
 * básica (título, descripción, versión) que se expone en la documentación
 * OpenAPI del servicio.
 */
@Configuration
public class SwaggerConfig {
    /**
     * Configures and customizes the OpenAPI specification for the Auxiliary Books Management API.
     *
     * @return Configured OpenAPI instance.
     */
    @Bean
    public OpenAPI customizeOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .info(new Info().title("Auxiliary Books Management API")
                        .description("API for Auxiliary Books management")
                        .version("1.0"));
    }
}
