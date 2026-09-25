package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.EnterpriseInfoDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;

import java.util.List;
import java.util.UUID;


/**
 * Puerto de entrada para la búsqueda y consulta de empresas.
 * Define las operaciones disponibles para la recuperación de información de empresas,
 * tanto activas como inactivas.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseSearchManagerPort {
    
    /**
     * Recupera todas las empresas activas registradas en el sistema.
     *
     * @return List<EnterpriseInfoDto> Lista de DTOs con la información básica de las empresas activas
     * @see EnterpriseInfoDto
     */
    List<EnterpriseInfoDto> getAllEnterprises();

    /**
     * Recupera todas las empresas inactivas registradas en el sistema.
     *
     * @return List<EnterpriseInfoDto> Lista de DTOs con la información básica de las empresas inactivas
     * @see EnterpriseInfoDto
     */
    List<EnterpriseInfoDto> getAllEnterprisesInactive();

    /**
     * Obtiene la información detallada de una empresa específica por su ID.
     *
     * @param id UUID identificador único de la empresa
     * @return Enterprise Objeto con la información completa de la empresa
     * @see Enterprise
     */
    Enterprise getEnterpriseById(UUID id);
}
