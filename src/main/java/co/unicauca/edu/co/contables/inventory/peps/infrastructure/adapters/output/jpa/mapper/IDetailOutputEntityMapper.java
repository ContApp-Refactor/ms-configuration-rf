package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;

@Mapper(componentModel = "spring")
/**
 * @brief Mapper for DetailOutput entity conversions
 * 
 * Handles transformation between DetailOutput domain model and DetailOutputEntity.
 */
public interface IDetailOutputEntityMapper {

    /**
     * @brief Converts entity to domain model
     * @param entity The DetailOutputEntity
     * @return DetailOutput domain object
     */
    @Mapping(target = "movementOrigin", ignore = true)
    @Mapping(target = "movementSale", ignore = true)
    DetailOutput toDomain(DetailOutputEntity entity);

  
    /**
     * @brief Converts domain model to entity
     * @param detailOutput The DetailOutput domain object
     * @return DetailOutputEntity
     */
    @Mapping(target = "movementSale", ignore = true)
    @Mapping(target = "movementOrigin", ignore = true)
    DetailOutputEntity toEntity(DetailOutput detailOutput);

    /**
     * @brief Converts list of domain models to list of entities
     * @param details List of DetailOutput domain objects
     * @return List of DetailOutputEntity
     */
    List<DetailOutputEntity> toEntityList(List<DetailOutput> details);

    /**
     * @brief Converts list of entities to list of domain models
     * @param entities List of DetailOutputEntity
     * @return List of DetailOutput domain objects
     */
    List<DetailOutput> toDomainList(List<DetailOutputEntity> entities);
}
