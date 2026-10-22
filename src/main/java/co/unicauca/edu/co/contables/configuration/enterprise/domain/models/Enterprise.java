package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * @brief Modelo de dominio para una empresa
 *
 * Contiene la información de identificación, contacto, ubicación y
 * características fiscales de una empresa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enterprise {
    private UUID id;
    private String idUser;
    private String name;
    private String nit;
    private String DV;
    private String phone;
    private String branch;
    private String email;
    private String logo;
    private Long mainActivity;
    private Long secondaryActivity;
    private StateEnum state;
    private InventoryConfigurationTypeEnum inventoryConfigurationType;
    private List<TaxLiability> taxLiabilities;
    private TaxPayerType taxPayerType;
    private EnterpriseType enterpriseType;
    private PersonType personType;
    private Location location;
}
