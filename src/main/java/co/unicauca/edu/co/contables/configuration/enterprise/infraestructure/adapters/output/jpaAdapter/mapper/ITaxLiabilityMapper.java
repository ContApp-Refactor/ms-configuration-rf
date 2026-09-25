package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Interfaz de mapeo para convertir entre entidades de responsabilidad fiscal y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface ITaxLiabilityMapper {
     
    /**
     * Convierte un objeto de dominio TaxLiability a su entidad correspondiente.
     *
     * @param taxLiability el objeto de dominio a convertir
     * @return la entidad TaxLiabilityEntity correspondiente
     */
    TaxLiabilityEntity toEntity(TaxLiability taxLiability);

    /**
     * Convierte una lista de entidades TaxLiabilityEntity a una lista de objetos de dominio.
     *
     * @param taxLiabilityEntities la lista de entidades a convertir
     * @return la lista de objetos de dominio TaxLiability correspondiente
     */
    List<TaxLiability> toModel(List<TaxLiabilityEntity> taxLiabilityEntities);
}
