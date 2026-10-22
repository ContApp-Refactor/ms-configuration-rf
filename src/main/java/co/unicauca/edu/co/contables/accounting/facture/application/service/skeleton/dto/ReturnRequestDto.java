package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.InventoryConfigurationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDto {
    
    @NotNull(message = "El código de factura es obligatorio")
    private Long factCode;
    
    @NotEmpty(message = "Debe incluir al menos un producto a devolver")
    @Valid
    private List<ProductReturnDto> products;
    
    @NotNull(message = "El tipo de configuración de inventario es obligatorio")
    private InventoryConfigurationType inventoryConfigType;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductReturnDto {
        
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productId;
        
        @NotNull(message = "La cantidad a devolver es obligatoria")
        private Integer quantity;
        
        private String reason;
    }
}
