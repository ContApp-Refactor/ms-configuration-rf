package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception;

import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.*;
import org.springframework.stereotype.Service;

/**
 * @brief Implementación del puerto de salida para formatear y lanzar respuestas de error.
 *
 * Provee métodos para lanzar las excepciones personalizadas ante
 * distintos escenarios: violación de reglas de negocio, conflictos
 * de existencia de entidades y errores genéricos, prefijando el
 * mensaje con la descripción del {@link ErrorCode} correspondiente.
 */
@Service
public class FormatterResultOutputPort implements IFormatterResultOutputPort {

    /**
     * @brief Lanza una BusinessRuleException para violaciones de reglas de negocio.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir en la excepción.
     */
    @Override
    public void returnBusinessRuleErrorResponse(int status, String message) {
        throw new BusinessRuleException(status, ErrorCode.BUSINESS_RULE_VIOLATION.getDescription()  + message);
    }

    /**
     * @brief Lanza una EntityAlreadyExists cuando la entidad ya existe.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir en la excepción.
     */
    @Override
    public void returnEntityAlreadyExistsErrorResponse(int status, String message) {
        throw new EntityAlreadyExists(status, ErrorCode.ENTITY_ALREADY_EXISTS.getDescription() + message);
    }

    /**
     * @brief Lanza una EntityDoesNotExistException cuando la entidad no existe.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir en la excepción.
     */
    @Override
    public void returnEntityDoesNotExistErrorResponse(int status, String message) {
        throw new EntityDoesNotExistException(status, ErrorCode.ENTITY_NOT_FOUND.getDescription() + message);
    }

    /**
     * @brief Lanza una GenericErrorException para errores genéricos.
     * @param status Código HTTP a retornar.
     * @param message Mensaje de error a incluir en la excepción.
     */
    @Override
    public void returnErrorGenericResponse(int status, String message) {
        throw new GenericErrorException(status, ErrorCode.GENERIC_ERROR.getDescription() + message);
    }

}
