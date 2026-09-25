package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IAddressSearchManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IAddressSearchOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que implementa las operaciones de búsqueda de direcciones.
 * Gestiona la lógica de negocio para la obtención de departamentos y sus ciudades asociadas,
 * actuando como intermediario entre los puertos de entrada y salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class AddressSearchService implements IAddressSearchManagerPort {

    /**
     * Puerto de salida para operaciones de búsqueda de direcciones.
     */
    private final IAddressSearchOutputPort addressSearchOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Department> getAllDepartment() {
        return addressSearchOutputPort.getAllDepartment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Department getAllCities(Long idDepartment) {
        return addressSearchOutputPort.getAllCities(idDepartment);
    }
}
