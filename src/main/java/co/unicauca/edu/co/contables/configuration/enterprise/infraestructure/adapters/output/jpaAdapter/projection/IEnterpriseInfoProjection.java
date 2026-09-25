package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.projection;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;

import java.util.UUID;

/**
 * Proyección de JPA para obtener información básica de una empresa.
 * Define los métodos para acceder a los campos seleccionados de la entidad Enterprise.
 */
public interface IEnterpriseInfoProjection {

    /**
     * Obtiene el identificador único de la empresa.
     *
     * @return el identificador de la empresa
     */
    UUID getId();

    /**
     * Obtiene el nombre de la empresa.
     *
     * @return el nombre de la empresa
     */
    String getName();

    /**
     * Obtiene el NIT de la empresa.
     *
     * @return el NIT de la empresa
     */
    String getNit();

    /**
     * Obtiene el logo de la empresa.
     *
     * @return el logo de la empresa
     */
    String getLogo();

    InventoryConfigurationTypeEnum getInventoryConfigurationType();
}
