package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;

import org.mapstruct.Mapper;

import java.util.List;

/**
 * @brief Mapeador de datos para operaciones de consulta de impuestos
 *
 * Gestiona la conversión de entidades JPA a modelos de dominio
 * para operaciones de lectura y búsqueda de impuestos.
 */
@Mapper
public interface ITaxSearchMapper {
    /**
     * @brief Convierte entidad JPA a modelo de dominio
     * @param taxEntity entidad con relaciones JPA cargadas
     * @return modelo de dominio con entidades relacionadas mapeadas
     */
    default Tax toDomain(TaxEntity taxEntity) {
        if (taxEntity == null) {
            return null;
        }
        return Tax.builder()
                .id(taxEntity.getId())
                .idEnterprise(taxEntity.getIdEnterprise())
                .code(taxEntity.getCode())
                .description(taxEntity.getDescription())
                .interest(taxEntity.getInterest())
                .purchaseTax(taxEntity.getPurchaseTax())
                .salesTax(taxEntity.getSalesTax())
                .status(taxEntity.getStatus())
                .usageCount(taxEntity.getUsageCount())
                .build();
    }

    /**
     * @brief Convierte lista de entidades JPA a lista de modelos de dominio
     * @param taxes lista de entidades con relaciones JPA cargadas
     * @return lista de modelos de dominio con entidades relacionadas mapeadas
     */
    default List<Tax> toDomainList(List<TaxEntity> taxes) {
        if (taxes == null) {
            return null;
        }
        return taxes.stream()
                .map(taxEntity -> {
                    Tax tax = Tax.builder()
                            .id(taxEntity.getId())
                            .code(taxEntity.getCode())
                            .idEnterprise(taxEntity.getIdEnterprise())
                            .description(taxEntity.getDescription())
                            .interest(taxEntity.getInterest())
                            .salesTax(taxEntity.getSalesTax())
                            .purchaseTax(taxEntity.getPurchaseTax())
                            .status(taxEntity.getStatus())
                            .usageCount(taxEntity.getUsageCount())
                            .build();
                    return tax;

                })
                .toList();
    }
}
