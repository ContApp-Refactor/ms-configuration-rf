package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.*;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.*;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Interfaz de mapeo para convertir modelos del dominio a entidades JPA durante actualizaciones.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface IEnterpriseUpdateMapper {
    
    /**
     * Convierte una lista de responsabilidades fiscales del dominio a entidades JPA.
     *
     * @param taxLiabilities lista de responsabilidades fiscales del dominio
     * @return lista de entidades TaxLiabilityEntity
     */
    List<TaxLiabilityEntity> toTaxLiabilityEntity(List<TaxLiability> taxLiabilities);

    /**
     * Convierte un tipo de contribuyente del dominio a entidad JPA.
     *
     * @param taxPayerType tipo de contribuyente del dominio
     * @return entidad TaxPayerTypeEntity
     */
    TaxPayerTypeEntity toTaxPayerTypeEntity(TaxPayerType taxPayerType);

    /**
     * Convierte un tipo de empresa del dominio a entidad JPA.
     *
     * @param enterpriseType tipo de empresa del dominio
     * @return entidad EnterpriseTypeEntity
     */
    EnterpriseTypeEntity toEnterpriseTypeEntity(EnterpriseType enterpriseType);

    /**
     * Convierte un tipo de persona del dominio a entidad JPA.
     *
     * @param personType tipo de persona del dominio
     * @return entidad PersonTypeEntity
     */
    PersonTypeEntity toPersonTypeEntity(PersonType personType);

    /**
     * Convierte una ubicación del dominio a entidad JPA.
     *
     * @param location ubicación del dominio
     * @return entidad LocationEntity
     */
    LocationEntity toLocationEntity(Location location);
}
