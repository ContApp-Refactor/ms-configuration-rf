package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.DepartmentEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;

import java.util.List;

/**
 * @brief Mapeador de datos para departamentos
 */
@Mapper(componentModel = "spring", uses = CountryDataMapper.class)
public interface DepartmentDataMapper {

    @Mapping(target = "cities", ignore = true)
    DepartmentEntity toEntity(Department domain);

    Department toDomain(DepartmentEntity entity);

    List<Department> toDomainList(List<DepartmentEntity> entities);
}
