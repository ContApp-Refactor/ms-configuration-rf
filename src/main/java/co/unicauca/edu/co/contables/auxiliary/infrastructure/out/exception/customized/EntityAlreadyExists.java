package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized;

/**
 * @brief Excepción que se lanza cuando una entidad ya existe.
 *
 * Indica que se intentó crear una entidad duplicada, típicamente
 * cuando un identificador o clave única ya está presente en la base.
 */
public class EntityAlreadyExists extends BaseException{

    /**
     * @brief Construye una EntityAlreadyExists con el código y mensaje indicados.
     * @param errorCode Código HTTP o código de error personalizado.
     * @param message Mensaje descriptivo de la excepción.
     */
    public EntityAlreadyExists(Integer errorCode, String message) {
        super(errorCode, message);
    }

}
