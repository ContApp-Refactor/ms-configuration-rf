package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief Clase base para las excepciones de runtime personalizadas de la aplicación.
 *
 * Provee una estructura común con código HTTP y mensaje para que las
 * subclases representen escenarios de error específicos del dominio
 * y la infraestructura.
 */
@Getter
@Setter
public abstract class BaseException extends RuntimeException {

    /**
     * @brief Código HTTP asociado a la excepción.
     */
    private Integer status;

    /**
     * @brief Mensaje descriptivo de la excepción.
     */
    private String message;

    /**
     * @brief Construye una nueva BaseException con el código y mensaje indicados.
     * @param status Código HTTP asociado a la excepción.
     * @param message Mensaje descriptivo de la excepción.
     */
    public BaseException(Integer status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}