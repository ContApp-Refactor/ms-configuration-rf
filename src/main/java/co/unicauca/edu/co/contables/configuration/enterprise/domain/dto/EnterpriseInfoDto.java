package co.unicauca.edu.co.contables.configuration.enterprise.domain.dto;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import lombok.*;

import java.util.UUID;

/**
 * @brief Proyección resumida de una empresa, usada en listados.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseInfoDto {
    private UUID id;
    private String name;
    private String nit;
    private String logo;
    private InventoryConfigurationTypeEnum inventoryConfigurationType;
}
