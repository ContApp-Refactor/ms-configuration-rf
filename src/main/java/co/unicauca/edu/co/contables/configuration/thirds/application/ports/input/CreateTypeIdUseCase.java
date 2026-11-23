package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;

/**
 * @brief Caso de uso para creación de tipos de identificación
 *
 * Permite crear nuevos tipos de identificación para terceros
 * en el sistema con validación de reglas de negocio.
 */
public interface CreateTypeIdUseCase {
    /**
     * @brief Crea un nuevo tipo de identificación
     * @param typeId El tipo de identificación a crear
     * @return El tipo de identificación creado
     */
    TypeId createTypeId(TypeId typeId);
} 
