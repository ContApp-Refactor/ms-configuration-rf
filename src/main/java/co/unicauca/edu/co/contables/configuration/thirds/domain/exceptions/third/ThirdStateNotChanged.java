package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando no se ha podido cambiar el estado de un tercero
 *
 * Se utiliza cuando una operación de cambio de estado (activación/inactivación)
 * de un tercero falla por razones técnicas o de validación de negocio.
 */
public class ThirdStateNotChanged extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para cambios de estado fallidos.
     */
    public ThirdStateNotChanged() {
        super(ThirdsErrorCode.THIRD_STATE_NOT_CHANGED);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe por qué falló el cambio
     */
    public ThirdStateNotChanged(String customMessage) {
        super(ThirdsErrorCode.THIRD_STATE_NOT_CHANGED, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe por qué falló el cambio
     * @param cause causa original del error que impidió el cambio de estado
     */
    public ThirdStateNotChanged(String customMessage, Throwable cause) {
        super(ThirdsErrorCode.THIRD_STATE_NOT_CHANGED, customMessage, cause);
    }
}
