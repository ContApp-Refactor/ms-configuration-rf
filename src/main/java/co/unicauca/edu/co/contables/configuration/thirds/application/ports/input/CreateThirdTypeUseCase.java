package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;

/**
 * @brief Caso de uso para creación de tipos de tercero
 *
 * Permite crear nuevos tipos de tercero en el sistema
 * con validación de reglas de negocio.
 */
public interface CreateThirdTypeUseCase {

    /**
     * @brief Crea un nuevo tipo de tercero
     * @param thirdType Tipo de tercero a crear
     * @return Tipo de tercero creado
     */
    ThirdType createThirdType(ThirdType thirdType);
    
} 
