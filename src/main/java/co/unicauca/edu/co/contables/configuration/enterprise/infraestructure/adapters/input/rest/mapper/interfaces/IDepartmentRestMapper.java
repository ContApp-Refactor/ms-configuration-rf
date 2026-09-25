package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface IDepartmentRestMapper {


    List<DepartmentAddressResponse> toDepartmentResponseList(List<Department> department);
}
