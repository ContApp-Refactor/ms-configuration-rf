package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;

/**
 * @brief MapStruct mapper for MessageProcessingError entity conversions
 * 
 * Handles conversion between domain model and JPA entity for
 * message processing error objects.
 */
@Mapper(componentModel = "spring")
public interface IMessageProcessingErrorEntityMapper {
    /**
     * @brief Converts JPA entity to domain model
     * @param entity MessageProcessingError entity
     * @return MessageProcessingError domain model
     */
    MessageProcessingError toDomain(MessageProcessingErrorEntity entity);
    
    /**
     * @brief Converts domain model to JPA entity
     * @param domain MessageProcessingError domain model
     * @return MessageProcessingError entity
     */
    //@Mapping(target = "tenantId", ignore = true)
    MessageProcessingErrorEntity toEntity(MessageProcessingError domain);
}
