package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;

import java.util.List;

/**
 * Puerto de salida para la búsqueda y consulta de direcciones geográficas.
 * Define las operaciones necesarias para obtener información sobre departamentos
 * y sus ciudades asociadas desde el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IAddressSearchOutputPort {

    /**
     * Recupera todos los departamentos disponibles en el sistema.
     *
     * @return List<Department> Lista de todos los departamentos registrados
     * @see Department
     */
    List<Department> getAllDepartment();

    /**
     * Obtiene la información de un departamento específico incluyendo sus ciudades.
     *
     * @param idDepartment Identificador único del departamento
     * @return Department Objeto que contiene la información del departamento y sus ciudades
     * @see Department
     */
    public Department getAllCities(Long idDepartment);
}
