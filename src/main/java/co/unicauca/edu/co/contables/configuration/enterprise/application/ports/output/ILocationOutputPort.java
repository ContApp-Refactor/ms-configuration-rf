package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;

/**
 * Puerto de salida para la gestión de ubicaciones.
 * Define las operaciones necesarias para persistir y eliminar ubicaciones
 * en el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ILocationOutputPort {
    
    /**
     * Crea una nueva ubicación en el sistema de almacenamiento.
     *
     * @param location Objeto Location con la información de la ubicación a crear
     * @return Location Objeto Location con la información de la ubicación creada
     * @see Location
     */
    Location create(Location location);

    /**
     * Elimina una ubicación existente del sistema de almacenamiento.
     *
     * @param id Identificador único de la ubicación a eliminar
     * @return boolean true si la eliminación fue exitosa, false en caso contrario
     */
    boolean delete(Long id);
}
