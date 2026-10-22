package co.unicauca.edu.co.contables.commons.config;

import co.unicauca.edu.co.contables.commons.multitenancy.interceptor.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @brief Configuración web de Spring para multi-tenancy
 *
 * Implementa WebMvcConfigurer para registrar interceptores web,
 * específicamente el TenantInterceptor que maneja la multi-tenancy
 * en las solicitudes HTTP.
 */
@RequiredArgsConstructor
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Value("${app.storage.logo-dir:uploads/enterprises/logos}")
    private String logoDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(tenantInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/logos/**")
                .addResourceLocations("file:" + logoDir + "/");
    }

}