package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Interfaz de mapeo para convertir entre entidades de país y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface ICountryMapper {
     
    /**
     * Convierte un objeto de dominio Country a su entidad correspondiente.
     *
     * @param country el objeto de dominio a convertir
     * @return la entidad CountryEntity correspondiente
     */
    CountryEntity toEntity(Country country);

    /**
     * Convierte una lista de entidades CountryEntity a una lista de objetos de dominio.
     *
     * @param countryEntities la lista de entidades a convertir
     * @return la lista de objetos de dominio Country correspondiente
     */
    List<Country> toModel(List<CountryEntity> countryEntities);
}