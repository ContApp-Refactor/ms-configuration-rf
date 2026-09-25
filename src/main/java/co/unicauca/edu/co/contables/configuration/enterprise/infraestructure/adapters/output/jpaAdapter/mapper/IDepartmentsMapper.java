package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaz de mapeo para convertir entre entidades de departamento y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface IDepartmentsMapper {

    /**
     * Convierte un objeto de dominio Department a su entidad correspondiente.
     *
     * @param department el objeto de dominio a convertir
     * @return la entidad DepartmentEntity correspondiente
     */
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "cities", ignore = true)
    DepartmentEntity toEntity(Department department);

    /**
     * Convierte una entidad DepartmentEntity a su representación en el dominio.
     *
     * @param departmentEntity la entidad a convertir
     * @return el objeto de dominio Department correspondiente
     */
    @Mapping(target = "department.country", ignore = true)
    @Mapping(target = "cities", ignore = true)
    Department toModel(DepartmentEntity departmentEntity);

    /**
     * Convierte una lista de objetos de dominio Department a una lista de entidades.
     *
     * @param departmentList la lista de objetos de dominio a convertir
     * @return la lista de entidades DepartmentEntity correspondiente
     */
    default List<DepartmentEntity> toEntityList(List<Department> departmentList) {
        if (departmentList == null) {
            return null;
        }
        return departmentList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de entidades DepartmentEntity a una lista de objetos de dominio.
     *
     * @param departmentEntityList la lista de entidades a convertir
     * @return la lista de objetos de dominio Department correspondiente
     */
    default List<Department> toModelList(List<DepartmentEntity> departmentEntityList) {
        if (departmentEntityList == null) {
            return null;
        }
        return departmentEntityList.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
