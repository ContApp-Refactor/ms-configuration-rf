package co.unicauca.edu.co.contables.auxiliary.domain.ports;

/**
 * @brief Puerto de salida para el servicio de internacionalización de mensajes.
 *
 * Expone la interfaz para obtener mensajes localizados con soporte de
 * sustitución de parámetros y mensaje por defecto.
 */
public interface IMessageServicePort {
    /**
     * @brief Obtiene un mensaje localizado por clave con parámetros.
     * @param key Identificador del mensaje.
     * @param args Parámetros para la interpolación del mensaje.
     * @return Mensaje localizado con los parámetros sustituidos.
     */
    public String getMessage(String key, Object... args);

    /**
     * @brief Obtiene un mensaje localizado con valor por defecto.
     * @param key Identificador del mensaje.
     * @param defaultMessage Mensaje por defecto si la clave no existe.
     * @param args Parámetros para la interpolación del mensaje.
     * @return Mensaje localizado o valor por defecto con parámetros sustituidos.
     */
    public String getMessage(String key, String defaultMessage, Object... args);
}
