package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;

/**
 * @brief Caso de uso para actualización de tipos de tercero
 *
 * Define el contrato para modificar tipos de tercero existentes
 * en el sistema con validación de integridad de datos.
 */
public interface UpdateThirdTypeUseCase {

    /**
     * @brief Actualiza un tipo de tercero existente
     * @param thirdType El tipo de tercero con los datos actualizados
     * @return El tipo de tercero actualizado
     */
    ThirdType updateThirdType(ThirdType thirdType);
}
