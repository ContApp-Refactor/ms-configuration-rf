package co.unicauca.edu.co.contables.inventory.products.domain.exception;

/**
 * @brief Interfaz para definición de códigos de error
 *
 * Define el contrato que deben implementar todos los códigos de error
 * en el sistema para acceso uniforme a código y mensaje.
 */
public interface ErrorCodeDefinition {

    String getCode();

    String getMessage();
}
