package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para módulo inválido
 *
 * Se lanza cuando se intenta usar un módulo inválido o inexistente
 * en las operaciones del sistema.
 */
public class InvalidModuleException extends BaseBusinessException {

    public InvalidModuleException(String moduleName) {
        super(DocumentTypesErrorCode.INVALID_MODULE,
                String.format("El módulo '%s' no es válido o no existe en el sistema", moduleName));
    }

    public InvalidModuleException(Integer moduleId) {
        super(DocumentTypesErrorCode.INVALID_MODULE,
                String.format("No existe un módulo con ID: %d", moduleId));
    }
}
