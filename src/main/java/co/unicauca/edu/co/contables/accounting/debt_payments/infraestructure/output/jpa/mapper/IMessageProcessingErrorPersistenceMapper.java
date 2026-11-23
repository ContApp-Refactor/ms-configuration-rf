package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IMessageProcessingErrorPersistenceMapper {
    MessageProcessingError toDomain(MessageProcessingErrorEntity entity);
    MessageProcessingErrorEntity toEntity(MessageProcessingError domain);
}
