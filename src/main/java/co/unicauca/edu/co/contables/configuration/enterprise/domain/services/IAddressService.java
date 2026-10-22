package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;

import java.util.List;

public interface IAddressService {
    List<Country> getAllCountries();

    List<Department> getAllDepartments(Long idCountry);

    Department getAllCities(Long idDepartment);
}
