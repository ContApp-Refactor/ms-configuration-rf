package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.MessageProcessingErrorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IMessageProcessingErrorRestMapper {

    MessageProcessingErrorResponse toResponse(MessageProcessingError domain);

}
