package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel="spring", uses={IDetailOutputEntityMapper.class})
/**
 * @brief Mapper for Kardex query operations
 * 
 * Handles conversion between KardexEntity and Kardex domain model for read operations.
 */
public interface IKardexEntityQueryMapper {
    
    /**
     * @brief Converts entity to domain model
     * @param kardexEntity The KardexEntity
     * @return Kardex domain object
     */
    @Mapping(target = "detailsOrigin", ignore=true)
    @Mapping(target = "detailsOutput", ignore=true)
    @Mapping(target = "product", ignore=true)
    Kardex toDomain(KardexEntity kardexEntity);

    /**
     * @brief Converts list of entities to list of domain models
     * @param KardexEntity List of KardexEntity
     * @return List of Kardex domain objects
     */
    List<Kardex> toDomainList(List<KardexEntity> KardexEntity);
}
