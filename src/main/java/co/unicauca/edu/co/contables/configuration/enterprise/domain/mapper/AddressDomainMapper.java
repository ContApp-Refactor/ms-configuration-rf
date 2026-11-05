package co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CitiesByDepartmentRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CountryRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.DepartmentRes;

import java.util.List;

/**
 * @brief Mapeador de dominio para direcciones (países, departamentos y
 * ciudades).
 */
@Mapper(componentModel = "spring")
public interface AddressDomainMapper {

    List<CountryRes> toCountryResList(List<Country> countries);

    List<DepartmentRes> toDepartmentResList(List<Department> departments);

    CitiesByDepartmentRes toCitiesByDepartmentRes(Department department);
}
