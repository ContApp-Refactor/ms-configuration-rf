package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookCriteriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper MapStruct entre los criterios de dominio y su entidad JPA.
 *
 * Convierte {@link AuxiliaryBookCriteria} a
 * {@link AuxiliaryBookCriteriaEntity} ignorando el id y la relación
 * inversa, y aplanando el rango de criterios en columnas separadas.
 */
@Mapper(componentModel = "spring")
public interface IAuxiliaryBookCriteriaCommandEntityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "auxiliaryBook", ignore = true)
    @Mapping(source = "criteriaType", target = "criteriaType")
    @Mapping(source = "criteriaRange.fromRange", target = "fromRange")
    @Mapping(source = "criteriaRange.toRange", target = "toRange")
    AuxiliaryBookCriteriaEntity toCriteriaEntity(AuxiliaryBookCriteria domain);
}
