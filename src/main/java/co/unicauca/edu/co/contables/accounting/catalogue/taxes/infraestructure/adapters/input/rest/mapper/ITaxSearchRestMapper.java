package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxSearchRes;

import org.mapstruct.Mapper;

import java.util.List;

/**
 * @brief Mapeador REST para operaciones de consulta de impuestos
 *
 * Gestiona la conversión de modelos de dominio a DTOs de respuesta
 * con formato especial para cuentas contables (códigos en lugar de IDs).
 */
@Mapper
public interface ITaxSearchRestMapper {
    /**
     * @brief Convierte modelo de dominio a response de búsqueda
     * @param tax modelo de dominio con entidades relacionadas cargadas
     * @return response con códigos de cuentas contables formateados
     */
    default TaxSearchRes toSearchResponse(Tax tax) {
        if (tax == null) {
            return null;
        }
        return TaxSearchRes.builder()
                .id(tax.getId())
                .idEnterprise(tax.getIdEnterprise())
                .code(tax.getCode())
                .description(tax.getDescription())
                .interest(tax.getInterest())
                .purchaseTax(tax.getPurchaseTax() != null ? tax.getPurchaseTax().getCode() : null)
                .salesTax(tax.getSalesTax() != null ? tax.getSalesTax().getCode() : null)
                .status(tax.getStatus())
                .usageCount(tax.getUsageCount())
                .build();
    }

    /**
     * @brief Convierte lista de modelos de dominio a lista de responses
     * @param taxes lista de modelos de dominio con entidades relacionadas
     * @return lista de responses con códigos de cuentas contables formateados
     */
    default List<TaxSearchRes> toSearchListResponse(List<Tax> taxes) {
        if (taxes == null) {
            return null;
        }
        return taxes.stream()
                .map(tax -> {
                    TaxSearchRes taxSearchRes = TaxSearchRes.builder()
                            .id(tax.getId())
                            .idEnterprise(tax.getIdEnterprise())
                            .code(tax.getCode())
                            .description(tax.getDescription())
                            .interest(tax.getInterest())
                            .salesTax(tax.getSalesTax() != null ? tax.getSalesTax().getCode() : null)
                            .purchaseTax(tax.getPurchaseTax() != null ? tax.getPurchaseTax().getCode() : null)
                            .status(tax.getStatus())
                            .usageCount(tax.getUsageCount())
                            .build();
                    return taxSearchRes;

                })
                .toList();

    }
}
