package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;

/**
 * Puerto de entrada para la gestión de ubicaciones.
 * Define las operaciones básicas para la creación y eliminación de ubicaciones
 * en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ILocationMangerPort {
    
    /**
     * Crea una nueva ubicación en el sistema.
     *
     * @param location Objeto Location con la información de la ubicación a crear
     * @return Location Objeto Location con la información de la ubicación creada
     * @see Location
     */
    Location createLocation(Location location);

    /**
     * Elimina una ubicación existente del sistema.
     *
     * @param id Identificador único de la ubicación a eliminar
     * @return boolean true si la eliminación fue exitosa, false en caso contrario
     */
    boolean deleteLocation(Long id);
}
