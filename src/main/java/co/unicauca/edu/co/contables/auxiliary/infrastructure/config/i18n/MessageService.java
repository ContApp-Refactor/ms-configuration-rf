package co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n;

import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * @brief Servicio de mensajes internacionalizados.
 *
 * Implementación de {@link IMessageServicePort} que resuelve claves de
 * mensaje usando el {@link MessageSource} de Spring y el locale activo
 * del {@link LocaleContextHolder}, con soporte de mensaje por defecto.
 */
@Service("auxiliaryMessageService")
@RequiredArgsConstructor
public class MessageService implements IMessageServicePort {

    private final MessageSource messageSource;

    /**
     * Gets a message using the current locale
     * @param key the message key
     * @param args arguments to format the message
     * @return the formatted message
     */
    @Override
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Gets a message with a default value if the key is not found
     * @param key the message key
     * @param defaultMessage default message
     * @param args arguments to format the message
     * @return the formatted message or the default message
     */
    @Override
    public String getMessage(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}
