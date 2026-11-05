package co.unicauca.edu.co.contables.notifications.infrastructure.input.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import co.unicauca.edu.co.contables.notifications.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.notifications.infrastructure.input.rest.dto.response.MessageProcessingErrorResponse;


@Mapper(componentModel = "spring", implementationName = "notifications<CLASS_NAME>Impl",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IMessageProcessingErrorRestMapper {

    MessageProcessingErrorResponse toResponse(MessageProcessingError domain);

}