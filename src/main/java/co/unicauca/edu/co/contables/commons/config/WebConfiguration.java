package co.unicauca.edu.co.contables.commons.config;

import co.unicauca.edu.co.contables.commons.multitenancy.interceptor.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private static final String LOGO_CSP = "default-src 'none'; style-src 'unsafe-inline'; sandbox";

    private final TenantInterceptor tenantInterceptor;

    @Value("${app.storage.logo-dir:uploads/enterprises/logos}")
    private String logoDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(tenantInterceptor);
        // Un logo SVG servido como image/svg+xml puede ejecutar scripts si alguien navega directo a la URL.
        // Esta CSP lo neutraliza (sin scripts, sin recursos externos, sandbox); en un <img> no tiene efecto.
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                response.setHeader("Content-Security-Policy", LOGO_CSP);
                return true;
            }
        }).addPathPatterns("/api/enterprises/logos/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cache desactivada: el resolver sniffea los primeros bytes y la cache de la cadena es innecesaria aqui.
        registry.addResourceHandler("/api/enterprises/logos/**")
                .addResourceLocations(logoLocation())
                .resourceChain(false)
                .addResolver(new LogoMediaTypeResourceResolver());
    }

    /** URI absoluta con barra final: funciona con rutas relativas, absolutas Linux o Windows. */
    private String logoLocation() {
        String uri = java.nio.file.Paths.get(logoDir).toAbsolutePath().normalize().toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

}
