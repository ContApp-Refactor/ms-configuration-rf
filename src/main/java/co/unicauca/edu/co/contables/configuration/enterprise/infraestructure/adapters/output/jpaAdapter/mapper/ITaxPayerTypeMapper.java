package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxPayerTypeEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Interfaz de mapeo para convertir entre entidades de tipo de contribuyente y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface ITaxPayerTypeMapper {
     
    /**
     * Convierte un objeto de dominio TaxPayerType a su entidad correspondiente.
     *
     * @param taxPayerType el objeto de dominio a convertir
     * @return la entidad TaxPayerTypeEntity correspondiente
     */
    TaxPayerTypeEntity toEntity(TaxPayerType taxPayerType);

    /**
     * Convierte una lista de entidades TaxPayerTypeEntity a una lista de objetos de dominio.
     *
     * @param taxPayerTypeEntities la lista de entidades a convertir
     * @return la lista de objetos de dominio TaxPayerType correspondiente
     */
    List<TaxPayerType> toModel(List<TaxPayerTypeEntity> taxPayerTypeEntities);
}