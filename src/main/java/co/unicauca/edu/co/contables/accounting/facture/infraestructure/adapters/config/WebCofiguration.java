package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.unicauca.edu.co.contables.commons.multitenancy.interceptor.TenantInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebCofiguration implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    /**
     * Agrega el interceptor del inquilino al registro, que es responsable de
     * establecer el inquilino en el contexto local de hilo basado en la solicitud
     * actual.
     * 
     * @param registry el registro al que se agrega el interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(tenantInterceptor);
    }

}
