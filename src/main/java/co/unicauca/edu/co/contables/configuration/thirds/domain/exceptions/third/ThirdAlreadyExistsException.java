package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta crear un tercero que ya existe
 *
 * Se utiliza durante operaciones de creación de terceros para prevenir
 * duplicados basados en el número de identificación (NIT, cédula, etc.).
 */
public class ThirdAlreadyExistsException extends BaseBusinessException {

    /**
     * @brief Constructor con número de identificación
     *
     * Crea una excepción específica cuando se detecta que ya existe un tercero
     * con el mismo número de identificación, generando automáticamente un mensaje descriptivo.
     * @param idNumber número de identificación del tercero que ya existe
     */
    public ThirdAlreadyExistsException(String idNumber) {
        super(ThirdsErrorCode.THIRD_ALREADY_EXISTS, "Ya existe un tercero con el número de identificación: " + idNumber);
    }
}
