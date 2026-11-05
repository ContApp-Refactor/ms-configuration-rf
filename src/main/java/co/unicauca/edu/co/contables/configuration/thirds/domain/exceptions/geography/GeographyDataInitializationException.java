package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando falla la inicialización de datos geográficos
 *
 * Esta excepción se utiliza específicamente durante el proceso de carga inicial
 * de países, estados y ciudades en el sistema. Puede ocurrir por problemas
 * de conectividad, archivos corruptos o errores en la estructura de datos.
 */
public class GeographyDataInitializationException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para fallos en la inicialización de datos geográficos.
     */
    public GeographyDataInitializationException() {
        super(GeographyErrorCode.GEOGRAPHY_DATA_INITIALIZATION_FAILED);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public GeographyDataInitializationException(String customMessage) {
        super(GeographyErrorCode.GEOGRAPHY_DATA_INITIALIZATION_FAILED, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public GeographyDataInitializationException(String customMessage, Throwable cause) {
        super(GeographyErrorCode.GEOGRAPHY_DATA_INITIALIZATION_FAILED, customMessage, cause);
    }

    /**
     * @brief Constructor con causa únicamente
     *
     * Crea una excepción con un mensaje estándar basado en la causa proporcionada.
     * Útil cuando se quiere preservar la información del error original.
     * @param cause causa original del error que provocó esta excepción
     */
    public GeographyDataInitializationException(Throwable cause) {
        super(GeographyErrorCode.GEOGRAPHY_DATA_INITIALIZATION_FAILED,
              "Error en la inicialización de datos geográficos: " + cause.getMessage(), cause);
    }
}
