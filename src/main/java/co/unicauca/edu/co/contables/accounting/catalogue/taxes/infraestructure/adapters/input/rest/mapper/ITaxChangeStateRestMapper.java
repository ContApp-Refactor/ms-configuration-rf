package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxChangeStateRes;

/**
 * @brief Mapeador REST para operaciones de cambio de estado de impuestos
 *
 * Gestiona la conversión de modelos de dominio a DTOs de respuesta
 * para operaciones de activación/desactivación con mensajes informativos.
 */
public interface ITaxChangeStateRestMapper {
    
    /**
     * @brief Convierte modelo de dominio a response de cambio de estado
     * @param tax modelo de dominio con estado actualizado
     * @return response con mensaje informativo según el estado
     */
    default TaxChangeStateRes toChangeStateResponse(Tax tax) {
        if (tax == null) {
            return null;
        }

        String statusMessage = tax.getStatus() ? "Impuesto activado exitosamente" : "Impuesto desactivado exitosamente";

        return TaxChangeStateRes.builder()
                .id(tax.getId())
                .code(tax.getCode())
                .description(tax.getDescription())
                .status(tax.getStatus())
                .usageCount(tax.getUsageCount())
                .message(statusMessage)
                .build();
    }
}
