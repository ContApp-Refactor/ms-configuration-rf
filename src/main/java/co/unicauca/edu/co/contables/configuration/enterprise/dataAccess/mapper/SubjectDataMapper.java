package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;

import java.util.List;

/**
 * @brief Mapeador de datos para materias (subjects)
 */
@Mapper(componentModel = "spring")
public interface SubjectDataMapper {

    @Mapping(target = "tenantId", ignore = true)
    SubjectEntity toEntity(Subject domain);

    Subject toDomain(SubjectEntity entity);

    List<Subject> toDomainList(List<SubjectEntity> entities);
}
