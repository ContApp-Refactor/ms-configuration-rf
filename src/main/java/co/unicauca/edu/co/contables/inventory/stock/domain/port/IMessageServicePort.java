package co.unicauca.edu.co.contables.inventory.stock.domain.port;

/**
 * @brief Output port for internationalization message service
 * 
 * Provides interface for retrieving localized messages
 * with parameter substitution support.
 */
public interface IMessageServicePort {
    /**
     * @brief Gets a localized message by key with parameters
     * @param key Message key identifier
     * @param args Parameters for message interpolation
     * @return Localized message with substituted parameters
     */
    public String getMessage(String key, Object... args);
    
    /**
     * @brief Gets a localized message with fallback default
     * @param key Message key identifier
     * @param defaultMessage Default message if key not found
     * @param args Parameters for message interpolation
     * @return Localized message or default with substituted parameters
     */
    public String getMessage(String key, String defaultMessage, Object... args);
}
