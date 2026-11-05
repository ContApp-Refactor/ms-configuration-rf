package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * @brief Configuration for application internationalization (i18n)
 * 
 * Enables multi-language support and locale configuration with
 * session-based locale resolution and dynamic language switching.
 */
@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    /**
     * @brief Configures MessageSource for loading localized message files
     * @return Configured message source with UTF-8 encoding and caching
     */
    @Bean
    MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH); // Default language
        return messageSource;
    }

    /**
     * @brief Configures LocaleResolver for determining current locale
     * @return Session-based locale resolver with English default
     */
    @Bean
    LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * @brief Creates interceptor for locale changes via request parameter
     * @return Locale change interceptor configured to use 'lang' parameter
     */
    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=es to switch to Spanish
        return interceptor;
    }

    /**
     * @brief Registers the locale change interceptor
     * @param registry Interceptor registry to add the locale interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
