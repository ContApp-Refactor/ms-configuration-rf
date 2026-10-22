package co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para eventos de uso de impuestos
 *
 * Contiene la información necesaria para actualizar el contador de uso
 * cuando otros servicios notifican que han utilizado un impuesto.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxUsageDto {
    private Long taxId;
    private String enterpriseId;
    private Integer quantityUsed;
}
