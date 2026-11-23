package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief Excepción que se lanza cuando se viola una regla de negocio.
 *
 * Indica que una restricción propia del dominio no se cumplió y que la
 * operación debe abortar con el código HTTP correspondiente.
 */
@Getter
@Setter
public class BusinessRuleException extends BaseException {

    /**
     * @brief Construye una BusinessRuleException con el código y mensaje indicados.
     * @param status Código HTTP asociado a la excepción.
     * @param message Mensaje que describe la regla de negocio violada.
     */
    public BusinessRuleException(Integer status, String message) {
        super(status, message);
    }
}
