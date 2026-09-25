package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.request;

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
public class UpdateInventoryConfigRequest {
       
    @NotNull(message = "El tipo de configuración de inventario es requerido")
    private InventoryConfigurationTypeEnum inventoryConfigType;
}
