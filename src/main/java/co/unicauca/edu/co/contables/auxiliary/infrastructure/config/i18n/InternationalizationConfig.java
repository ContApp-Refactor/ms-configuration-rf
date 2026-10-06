package co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * @brief Configuración de internacionalización (i18n) de la aplicación.
 *
 * Configura el {@link MessageSource} que carga los archivos messages_*,
 * el resolver de locale por sesión y el interceptor que permite cambiar
 * el idioma mediante el parámetro {@code ?lang=...} en las peticiones.
 */
@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    /**
     * Configura el MessageSource para cargar los archivos de mensajes
     */
    @Bean
    MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache por 1 hora
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH); // Idioma por defecto
        return messageSource;
    }

    /**
     * Configura el LocaleResolver para determinar el locale actual
     */
    @Bean
    LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * Interceptor para cambiar el locale basado en un parámetro de request
     */
    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=es para cambiar a español
        return interceptor;
    }

    /**
     * Registra el interceptor de cambio de locale
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
