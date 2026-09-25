package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.City;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaz de mapeo para convertir entre entidades de departamento y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface ICitiesbyDepartmentMapper {

    /**
     * Convierte un objeto de dominio Department a su entidad correspondiente.
     *
     * @param department el objeto de dominio a convertir
     * @return la entidad DepartmentEntity correspondiente
     */
    @Mapping(target = "cities", qualifiedByName = "mapCities")
    @Mapping(target = "country", ignore = true)
    DepartmentEntity toEntity(Department department);

    /**
     * Convierte una entidad DepartmentEntity a su representación en el dominio.
     *
     * @param departmentEntity la entidad a convertir
     * @return el objeto de dominio Department correspondiente
     */
    @Mapping(target = "cities", qualifiedByName = "mapCitiesDomain")
    Department toDomain(DepartmentEntity departmentEntity);

    /**
     * Método personalizado para mapear la lista de ciudades del dominio a entidades.
     *
     * @param cities lista de ciudades del dominio
     * @return lista de entidades CityEntity
     */
    @Named("mapCities")
    default List<CityEntity> mapCities(List<City> cities) {
        if (cities == null) {
            return null;
        }
        return cities.stream()
                .map(city -> {
                    CityEntity cityEntity = new CityEntity();
                    cityEntity.setId(city.getId());
                    cityEntity.setName(city.getName());
                    return cityEntity;
                })
                .collect(Collectors.toList());
    }

    /**
     * Método personalizado para mapear la lista de entidades de ciudad al dominio.
     *
     * @param cities lista de entidades CityEntity
     * @return lista de ciudades del dominio
     */
    @Named("mapCitiesDomain")
    default List<City> mapCitiesDomain(List<CityEntity> cities) {
        if (cities == null) {
            return null;
        }
        return cities.stream()
                .map(cityEntity -> {
                    City city = new City();
                    city.setId(cityEntity.getId());
                    city.setName(cityEntity.getName());
                    return city;
                })
                .collect(Collectors.toList());
    }
}
