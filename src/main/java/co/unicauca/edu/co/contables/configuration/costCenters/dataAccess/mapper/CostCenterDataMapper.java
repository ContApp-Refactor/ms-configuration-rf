package co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.mapper;

import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @brief Mapeador de datos para centros de costo
 *
 * Mapeador MapStruct que convierte entre entidades JPA y modelos de dominio
 * para centros de costo, manejando relaciones jerárquicas.
 */
@Mapper(componentModel = "spring")
public interface CostCenterDataMapper {

    @Mappings({
            @Mapping(target = "parent", ignore = true),
            @Mapping(target = "tenantId", ignore = true)
    })
    CostCenterEntity toEntity(CostCenter costCenter);

    @Mappings({
            @Mapping(target = "children", ignore = true),
            @Mapping(target = "parent", expression = "java(entity.getParent() != null ? CostCenter.builder().id(entity.getParent().getId()).build() : null)")
    })
    CostCenter toDomain(CostCenterEntity entity);
}
