package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;

/**
 * Puerto de entrada para la gestión de tipos de persona.
 * Define las operaciones básicas para la creación de diferentes tipos
 * de persona en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IPersonTypeManagerPort {
    
    /**
     * Crea un nuevo tipo de persona en el sistema.
     *
     * @param personType Objeto PersonType con la información del tipo de persona a crear
     * @return PersonType Objeto PersonType con la información del tipo de persona creado
     * @see PersonType
     */
    PersonType createPersonType(PersonType personType);
}
