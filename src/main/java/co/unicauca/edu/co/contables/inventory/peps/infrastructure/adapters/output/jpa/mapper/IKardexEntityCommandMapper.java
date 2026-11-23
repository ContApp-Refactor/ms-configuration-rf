package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel="spring", uses={IDetailOutputEntityMapper.class})
/**
 * @brief Mapper for Kardex command operations
 * 
 * Handles conversion between Kardex domain model and KardexEntity for write operations.
 */
public interface IKardexEntityCommandMapper {
    
    /**
     * @brief Converts domain model to entity
     * @param kardex The Kardex domain object
     * @return KardexEntity
     */
    @Mapping(target = "detailsOutput", ignore = true)
    @Mapping(target = "detailsOrigin", ignore = true)
    @Mapping(target = "product", ignore = true)
    KardexEntity toEntity(Kardex kardex);

    /**
     * @brief Converts entity to domain model
     * @param kardexEntity The KardexEntity
     * @return Kardex domain object
     */
    @Mapping(target = "detailsOutput", ignore = true)
    @Mapping(target = "detailsOrigin", ignore = true)
    @Mapping(target = "product", ignore = true)
    Kardex toDomain(KardexEntity kardexEntity);

    /**
     * @brief Converts domain model to entity specifically for sale registration
     * @param kardex The Kardex domain object
     * @return KardexEntity ready for sale persistence
     */
    // Método específico para mapear Kardex con detalles para registro de ventas
    @Mapping(target = "product", ignore = true)
    KardexEntity toEntityForSale(Kardex kardex);

    /**
     * @brief Converts list of entities to list of domain models
     * @param listKardex List of KardexEntity
     * @return List of Kardex domain objects
     */
    List<Kardex> listToDomain(List<KardexEntity> listKardex);
}

