package co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapeador de datos para entidades de bancos
 *
 * Gestiona la conversión entre entidades de base de datos y modelos de dominio,
 * manejando campos específicos como tenantId.
 */
@Mapper(componentModel = "spring")
public interface BankDataMapper {
    
    /**
     * @brief Convierte entidad de base de datos a modelo de dominio
     * @param entity Entidad de banco con relaciones cargadas
     * @return Modelo de dominio de banco
     */
    Bank toDomain(BankEntity entity);

    /**
     * @brief Convierte modelo de dominio a entidad de base de datos
     * @param domain Modelo de dominio de banco
     * @return Entidad preparada para persistencia (tenantId se establece en runtime)
     */
    @Mapping(target = "tenantId", ignore = true)
    BankEntity toEntity(Bank domain);
}
