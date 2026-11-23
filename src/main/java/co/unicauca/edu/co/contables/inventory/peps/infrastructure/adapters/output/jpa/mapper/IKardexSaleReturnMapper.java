package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel="spring")
/**
 * @brief Mapper for Kardex Sale Return operations
 * 
 * Specialized mapper for converting Kardex entities in the context of sale returns.
 */
public interface IKardexSaleReturnMapper {

    /**
     * @brief Converts entity to domain model ignoring relationships
     * @param kardexEntity The KardexEntity
     * @return Kardex domain object
     */
    @Mapping(target = "detailsOrigin", ignore=true)
   @Mapping(target = "detailsOutput", ignore=true)
   @Mapping(target = "product", ignore=true)
    Kardex toDomain(KardexEntity kardexEntity);


    
}
