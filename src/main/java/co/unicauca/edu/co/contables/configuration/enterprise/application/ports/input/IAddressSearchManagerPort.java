package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;

import java.util.List;

/**
 * Puerto de entrada para la gestión y búsqueda de direcciones geográficas.
 * Define las operaciones disponibles para la consulta de departamentos y sus ciudades asociadas.
 *
 * @author [CONTAPP]
 * @version 1.0
 * @since 1.0.0
 */
public interface IAddressSearchManagerPort {

    /**
     * Recupera el listado completo de departamentos disponibles en el sistema.
     *
     * @return List<Department> Colección de departamentos registrados en el sistema
     * @see Department
     */
    List<Department> getAllDepartment();

    /**
     * Obtiene la información detallada de un departamento específico incluyendo sus ciudades.
     *
     * @param idDepartment Identificador único del departamento a consultar
     * @return Department Objeto que contiene la información del departamento y su listado de ciudades
     * @see Department
     */
    public Department getAllCities(Long idDepartment);
}
