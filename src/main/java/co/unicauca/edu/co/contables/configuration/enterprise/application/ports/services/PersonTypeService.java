package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IPersonTypeManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IPersonTypeOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio que implementa las operaciones de gestión de tipos de persona.
 * Gestiona la lógica de negocio para la creación de diferentes tipos
 * de persona en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class PersonTypeService implements IPersonTypeManagerPort {

    /**
     * Puerto de salida para operaciones de gestión de tipos de persona.
     */
    private final IPersonTypeOutputPort personTypeOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public PersonType createPersonType(PersonType personType) {
        return personTypeOutputPort.create(personType);
    }
}
