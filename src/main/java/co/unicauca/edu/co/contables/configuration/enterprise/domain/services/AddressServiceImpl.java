package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.CountryDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.DepartmentDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseCountryRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.DepartmentRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @brief Implementación del servicio de dominio para países, departamentos y
 * ciudades asociadas a la ubicación de una empresa.
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {

    private final EnterpriseCountryRepository countryRepository;
    private final DepartmentRepository departmentRepository;
    private final CountryDataMapper countryDataMapper;
    private final DepartmentDataMapper departmentDataMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        return countryDataMapper.toDomainList(countryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentDataMapper.toDomainList(departmentRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Department getAllCities(Long idDepartment) {
        return departmentDataMapper.toDomain(departmentRepository.findById(idDepartment).orElse(null));
    }
}
