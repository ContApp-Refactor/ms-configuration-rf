package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;

import java.util.List;

/**
 * Puerto de entrada para la gestión de tipos de empresa.
 * Define las operaciones disponibles para la consulta y gestión de
 * los diferentes tipos de empresa en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseTypeManagerPort {
     
    /**
     * Recupera todos los tipos de empresa registrados en el sistema.
     *
     * @return List<EnterpriseType> Lista de todos los tipos de empresa disponibles
     * @see EnterpriseType
     */
    List<EnterpriseType> getAllEnterpriseTypes();  
}