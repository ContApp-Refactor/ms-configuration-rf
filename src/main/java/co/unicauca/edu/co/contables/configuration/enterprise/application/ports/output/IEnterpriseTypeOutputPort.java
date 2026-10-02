package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;

import java.util.List;

/**
 * Puerto de salida para la consulta de tipos de empresa.
 * Define las operaciones necesarias para recuperar información sobre
 * los tipos de empresa desde el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseTypeOutputPort {
     
    /**
     * Recupera todos los tipos de empresa registrados en el sistema.
     *
     * @return List<EnterpriseType> Lista de todos los tipos de empresa disponibles
     * @see EnterpriseType
     */
    List<EnterpriseType> getAllEnterpriseTypes();
}