package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.CitiesbyDepartmentResponse;
import org.mapstruct.Mapper;

@Mapper
public interface ICitiesbyDepartmentRestMapper {
    CitiesbyDepartmentResponse toResponse(Department department);
}
