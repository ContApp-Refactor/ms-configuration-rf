package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.MessageProcessingErrorDtoResponse;

/**
 * @brief MapStruct mapper for MessageProcessingError REST responses
 * 
 * Handles conversion between domain model and REST response DTOs
 * for message processing error operations.
 */
@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IMessageProcessingErrorResponseMapper {
    
    /**
     * @brief Converts domain model to response DTO
     * @param messageProcessingError Domain model
     * @return Response DTO
     */
    MessageProcessingErrorDtoResponse toDtoResponse(MessageProcessingError messageProcessingError);
}