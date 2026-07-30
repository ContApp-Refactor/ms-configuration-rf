package co.unicauca.edu.co.contables.configuration.costCenters.domain.mapper;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.response.CostCenterRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @brief Mapeador de dominio para centros de costo
 *
 * Mapeador MapStruct que convierte entre modelos de dominio y DTOs
 * de presentación para centros de costo.
 */
@Mapper(componentModel = "spring")
public interface CostCenterDomainMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "children", ignore = true),
            @Mapping(target = "parent", expression = "java(request.getParentId() != null ? CostCenter.builder().id(request.getParentId()).build() : null)")
    })
    CostCenter toDomain(CostCenterCreateReq request);

    @Mappings({
            @Mapping(target = "children", ignore = true),
            @Mapping(target = "parent", expression = "java(request.getParentId() != null ? CostCenter.builder().id(request.getParentId()).build() : null)")
    })
    CostCenter toDomain(CostCenterUpdateReq request);

    @Mappings({
            @Mapping(target = "parentId", source = "parent.id")
    })
    CostCenterRes toRes(CostCenter domain);
}


