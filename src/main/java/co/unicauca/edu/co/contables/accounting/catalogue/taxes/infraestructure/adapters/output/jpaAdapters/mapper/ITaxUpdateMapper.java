package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper;


import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;

import org.mapstruct.Mapper;

/**
 * @brief Mapeador de datos para operaciones de actualización de impuestos
 *
 * Gestiona la conversión automática entre entidades JPA y modelos de dominio
 * para operaciones de modificación de impuestos existentes.
 */
@Mapper(componentModel = "spring")
public interface ITaxUpdateMapper {
    /**
     * @brief Convierte modelo de dominio a entidad JPA automáticamente
     * @param tax modelo de dominio a convertir
     * @return entidad JPA con mapeo automático de campos
     */
    TaxEntity toEntity(Tax tax);

    /**
     * @brief Convierte entidad JPA a modelo de dominio automáticamente
     * @param taxEntity entidad JPA a convertir
     * @return modelo de dominio con mapeo automático de campos
     */
    Tax toModel(TaxEntity taxEntity);
}
