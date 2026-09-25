package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;

/**
 * Puerto de entrada para la gestión de creación de empresas.
 * Define las operaciones necesarias para el registro y creación de nuevas empresas en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseCreateMannegerPort {
    
    /**
     * Crea una nueva empresa en el sistema.
     *
     * @param enterprise Objeto Enterprise con la información de la empresa a crear
     * @return Enterprise Objeto Enterprise con la información de la empresa creada, incluyendo su ID
     * @see Enterprise
     */
    Enterprise createEnterprise(Enterprise enterprise);
}
