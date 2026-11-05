package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * @brief Respuesta con la información completa de una empresa.
 *
 * Se usa tanto para la creación como para la consulta por id.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseRes {
    private UUID id;
    private String name;
    private String nit;
    private String DV;
    private String phone;
    private String branch;
    private String email;
    private String logo;
    private StateEnum state;
    private InventoryConfigurationTypeEnum inventoryConfigurationType;
    private Long mainActivity;
    private Long secondaryActivity;
    private List<TaxLiability> taxLiabilities;
    private TaxPayerType taxPayerType;
    private EnterpriseType enterpriseType;
    private PersonType personType;
    private LocationRes location;
}
