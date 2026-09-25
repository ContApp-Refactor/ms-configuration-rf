package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.ILocationMangerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.ILocationOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio que implementa las operaciones de gestión de ubicaciones.
 * Gestiona la lógica de negocio para la creación y eliminación de ubicaciones
 * en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class LocationService implements ILocationMangerPort {

    /**
     * Puerto de salida para operaciones de gestión de ubicaciones.
     */
    private final ILocationOutputPort locationOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public Location createLocation(Location location) {
        return locationOutputPort.create(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteLocation(Long id) {
        return locationOutputPort.delete(id);
    }
}
