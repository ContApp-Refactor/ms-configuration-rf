package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryConfigReq {

    @NotNull(message = "El tipo de configuración de inventario es requerido")
    private InventoryConfigurationTypeEnum inventoryConfigType;
}
