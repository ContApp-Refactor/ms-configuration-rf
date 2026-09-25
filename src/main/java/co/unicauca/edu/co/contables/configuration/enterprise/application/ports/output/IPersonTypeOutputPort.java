package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;

/**
 * Puerto de salida para la gestión de tipos de persona.
 * Define las operaciones necesarias para persistir tipos de persona
 * en el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IPersonTypeOutputPort {
    
    /**
     * Crea un nuevo tipo de persona en el sistema de almacenamiento.
     *
     * @param personType Objeto PersonType con la información del tipo de persona a crear
     * @return PersonType Objeto PersonType con la información del tipo de persona creado
     * @see PersonType
     */
    PersonType create(PersonType personType);
}
