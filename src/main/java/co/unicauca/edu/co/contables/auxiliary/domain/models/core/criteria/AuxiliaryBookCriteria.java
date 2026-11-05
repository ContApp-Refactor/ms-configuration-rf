package co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @brief Modelo de dominio con los criterios de generación de un libro auxiliar.
 *
 * Agrupa los filtros aplicables al generar un libro: tipo de nivel y rango
 * de cuentas, centro de costo, tercero, tipo de comprobante y rango de
 * fechas (startDate/endDate). Ofrece utilidades para verificar la presencia
 * de rango y obtener una representación textual de los criterios.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuxiliaryBookCriteria {
    private Long id;

    private ECriteriaType criteriaType;

    private CriteriaRange criteriaRange;

    private String costCenterId;
    private String thirdPartyId;

    private String voucherType;

    private LocalDate startDate;
    private LocalDate endDate;

    public boolean hasRange() {
        return criteriaRange != null;
    }

    @Override
    public String toString(){
        return "Tipo de criterio: " + criteriaType + ", Rango: " + (hasRange() ? criteriaRange.toString() : "N/A") +
                ", Centro de costo: " + (costCenterId != null ? costCenterId : "N/A") +
                ", Tercero: " + (thirdPartyId != null ? thirdPartyId : "N/A") +
                ", Tipo de comprobante: " + (voucherType != null ? voucherType : "N/A") +
                ", Fecha inicio: " + (startDate != null ? startDate.toString() : "N/A") +
                ", Fecha fin: " + (endDate != null ? endDate.toString() : "N/A");
    }
}
