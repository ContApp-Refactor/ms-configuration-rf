package co.unicauca.edu.co.contables.configuration.commons.exceptions;

/**
 * @brief Contrato para códigos de error
 *
 * Contrato común para cualquier código de error de la aplicación.
 * Permite que múltiples catálogos (por dominio) sean usados de forma uniforme.
 */
public interface ErrorCodeDefinition {


    String getCode();

 
    String getMessage();
}


