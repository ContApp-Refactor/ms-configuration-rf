package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;

@Mapper(componentModel="spring", uses={IKardexSaleReturnMapper.class})
/**
 * @brief Mapper for DetailOutput in Sale Return context
 * 
 * Specialized mapper for handling DetailOutput conversions during sale return operations.
 */
public interface IDetailOutPutSaleReturnMapper {

    /**
     * @brief Converts entity to domain model
     * @param entity The DetailOutputEntity
     * @return DetailOutput domain object
     */
    DetailOutput toDomain(DetailOutputEntity entity);
    
    /**
     * @brief Converts list of entities to list of domain models
     * @param entities List of DetailOutputEntity
     * @return List of DetailOutput domain objects
     */
    List<DetailOutput> toDomainList(List<DetailOutputEntity> entities);
    
    /**
     * @brief Converts domain model to entity
     * @param detailOutput The DetailOutput domain object
     * @return DetailOutputEntity
     */
    @Mapping(target = "movementSale", ignore = true)
    @Mapping(target = "movementOrigin", ignore = true)
    DetailOutputEntity toEntity(DetailOutput detailOutput);

}
