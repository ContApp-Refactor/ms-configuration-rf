package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;

import java.util.List;

/**
 * @brief Mapeador de datos para las responsabilidades tributarias
 */
@Mapper(componentModel = "spring")
public interface TaxLiabilityDataMapper {
    TaxLiabilityEntity toEntity(TaxLiability domain);

    TaxLiability toDomain(TaxLiabilityEntity entity);

    List<TaxLiabilityEntity> toEntityList(List<TaxLiability> domain);

    List<TaxLiability> toDomainList(List<TaxLiabilityEntity> entities);
}
