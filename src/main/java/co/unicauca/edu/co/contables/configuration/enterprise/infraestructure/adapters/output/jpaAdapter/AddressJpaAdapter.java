package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IAddressSearchOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ICitiesbyDepartmentMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IDepartmentsMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IAddressRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IDepartmentAddressRepository;
import lombok.Data;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador para la búsqueda de direcciones usando JPA.
 * Implementa la interfaz IAddressSearchOutputPort.
 */
@Component
@Data
public class AddressJpaAdapter implements IAddressSearchOutputPort {

    private final IAddressRepository addressRepository;
    private final IDepartmentAddressRepository departmentAddressRepository;
    private final ICitiesbyDepartmentMapper citiesbyDepartmentMapper;
    private final IDepartmentsMapper departmentsMapper;

    /**
     * Obtiene todos los departamentos.
     *
     * @return una lista de todos los departamentos en el modelo de dominio
     */
    @Override
    public List<Department> getAllDepartment(){
        return departmentsMapper.toModelList(departmentAddressRepository.findAll());
    }

    /**
     * Obtiene todas las ciudades de un departamento específico.
     *
     * @param idDepartment el ID del departamento
     * @return el modelo de dominio del departamento
     */
    @Override
    public Department getAllCities(@NonNull Long idDepartment) {
       return citiesbyDepartmentMapper.toDomain(departmentAddressRepository.findById(idDepartment).get());
    }


}
