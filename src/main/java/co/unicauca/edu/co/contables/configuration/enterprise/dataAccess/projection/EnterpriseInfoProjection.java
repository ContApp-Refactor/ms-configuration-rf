package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.projection;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;

import java.util.UUID;

/**
 * @brief Proyección JPA con la información resumida de una empresa.
 */
public interface EnterpriseInfoProjection {
    UUID getId();

    String getName();

    String getNit();

    String getLogo();

    InventoryConfigurationTypeEnum getInventoryConfigurationType();
}
