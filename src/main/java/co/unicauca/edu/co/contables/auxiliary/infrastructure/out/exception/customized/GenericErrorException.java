package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized;

/**
 * @brief Excepción para errores genéricos o no categorizados.
 *
 * Se utiliza para indicar condiciones de error no específicas que
 * deben ser reportadas con un código HTTP y un mensaje descriptivo.
 */
public class GenericErrorException extends BaseException {

    /**
     * @brief Construye una GenericErrorException con el código y mensaje indicados.
     * @param status Código HTTP asociado a la excepción.
     * @param message Mensaje descriptivo del error.
     */
    public GenericErrorException(Integer status, String message) {
        super(status, message);
    }

}