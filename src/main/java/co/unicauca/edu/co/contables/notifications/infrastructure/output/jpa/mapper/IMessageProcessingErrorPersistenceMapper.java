package co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import co.unicauca.edu.co.contables.notifications.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.MessageProcessingErrorEntity;

@Mapper(componentModel = "spring", implementationName = "notifications<CLASS_NAME>Impl",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IMessageProcessingErrorPersistenceMapper {
    MessageProcessingError toDomain(MessageProcessingErrorEntity entity);
    MessageProcessingErrorEntity toEntity(MessageProcessingError domain);
}
