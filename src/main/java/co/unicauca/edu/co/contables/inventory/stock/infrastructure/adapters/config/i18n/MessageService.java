package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;

import lombok.RequiredArgsConstructor;

/**
 * @brief Service implementation for internationalized message management
 * 
 * Provides methods to retrieve localized messages with the current
 * locale context and parameter substitution support.
 */
@Service
@RequiredArgsConstructor
public class MessageService implements IMessageServicePort {
    
    private final MessageSource messageSource;

    /**
     * @brief Gets a message using the current locale
     * @param key Message key identifier
     * @param args Arguments for message formatting
     * @return Formatted localized message
     */
    @Override
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * @brief Gets a message with default fallback if key not found
     * @param key Message key identifier
     * @param defaultMessage Default message if key not found
     * @param args Arguments for message formatting
     * @return Formatted message or default message
     */
    @Override
    public String getMessage(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}
