package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.InventoryConfigurationType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonFactureRequestDto {
    
    @NotNull(message = "El código de factura es obligatorio")
    private Long factCode;
    
    @NotBlank(message = "El ID de entidad es obligatorio")
    private String entId;
    
    @NotNull(message = "El ID de tercero es obligatorio")
    private Long thId;
    
    @NotEmpty(message = "La factura debe tener al menos un producto")
    @Valid
    private Set<SkeletonProductRequestDto> products;
    
    @NotBlank(message = "El valor total es obligatorio")
    private String totalValue;
    
    @NotBlank(message = "El valor pagado es obligatorio")
    private String totalPay;
    
    @NotBlank(message = "El valor pendiente es obligatorio")
    private String pendingValue;

    private LocalDate issueDate;
    
    private LocalDate expirationDate;
    
    private Long accountingAccount;
    
    @NotNull(message = "El tipo de factura es obligatorio")
    private SkeletonFactureType factureType;
    
    @NotNull(message = "El tipo de configuración de inventario es obligatorio")
    private InventoryConfigurationType inventoryConfigType;

    private String TagTitle;
}
