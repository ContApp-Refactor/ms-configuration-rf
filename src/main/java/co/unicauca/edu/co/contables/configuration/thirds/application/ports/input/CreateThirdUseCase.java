package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;

/**
 * @brief Caso de uso para creación de terceros con validación geográfica
 *
 * Permite crear nuevos terceros en el sistema con validación
 * automática de la información geográfica proporcionada.
 */
public interface CreateThirdUseCase {
    /**
     * @brief Crea un nuevo tercero con validación geográfica
     * @param third el tercero a crear
     * @param countryCode código del país
     * @param stateCode código del estado/departamento
     * @param cityCode código de la ciudad
     * @return el tercero creado con geografía validada
     */
    Third createThird(Third third, String countryCode, String stateCode, String cityCode);
}
