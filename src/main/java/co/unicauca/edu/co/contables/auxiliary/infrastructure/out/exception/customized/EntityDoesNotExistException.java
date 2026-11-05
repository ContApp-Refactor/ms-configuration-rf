package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized;

import lombok.Getter;

/**
 * @brief Excepción que se lanza cuando una entidad no existe.
 *
 * Indica que la entidad solicitada no pudo ser encontrada en el
 * sistema de persistencia o en un servicio externo consultado.
 */
@Getter
public class EntityDoesNotExistException extends BaseException {

    /**
     * @brief Construye una EntityDoesNotExistException con el código y mensaje indicados.
     * @param status Código HTTP asociado a la excepción.
     * @param message Mensaje descriptivo de la excepción.
     */
    public EntityDoesNotExistException(Integer status, String message) {
        super(status, message);
    }

}
