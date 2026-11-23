package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;

/**
 * @brief Caso de uso para actualización de terceros con validación geográfica
 *
 * Permite modificar datos de terceros existentes con validación
 * opcional de información geográfica.
 */
public interface UpdateThirdUseCase {
    /**
     * @brief Actualiza tercero con validación opcional de geografía
     *
     * Modifica un tercero existente en el sistema con opción
     * de validar y actualizar información geográfica.
     *
     * @param third el tercero con los datos actualizados
     * @param countryCode código del país (opcional, puede ser null)
     * @param stateCode código del estado/departamento (opcional, puede ser null)
     * @param cityCode código de la ciudad (opcional, puede ser null)
     * @return el tercero actualizado
     */
    Third updateThirdWithGeography(Third third, String countryCode, String stateCode, String cityCode);
}
