package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexBatchDtoRequest;

/**
 * @brief Mapper for converting external kardex DTOs to domain models
 */
@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IKardexExternalClientMapper {
    
    /**
     * @brief Converts external DTO to domain model
     * @param dto External kardex DTO
     * @return Domain kardex model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    Kardex toDomain(KardexBatchDtoRequest dto);
}
