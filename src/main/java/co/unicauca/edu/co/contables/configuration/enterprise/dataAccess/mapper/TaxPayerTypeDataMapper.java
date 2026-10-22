package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxPayerTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;

import java.util.List;

/**
 * @brief Mapeador de datos para el tipo de contribuyente
 */
@Mapper(componentModel = "spring")
public interface TaxPayerTypeDataMapper {
    TaxPayerTypeEntity toEntity(TaxPayerType domain);

    TaxPayerType toDomain(TaxPayerTypeEntity entity);

    List<TaxPayerType> toDomainList(List<TaxPayerTypeEntity> entities);
}
