package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.PayableWriteOffEntity;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IPayableWriteOffPersistenceMapper {
    PayableWriteOff toDomain(PayableWriteOffEntity entity);
    List<PayableWriteOff> toDomainList(List<PayableWriteOffEntity> entities);
    PayableWriteOffEntity toEntity(PayableWriteOff domain);

    @AfterMapping
    default void linkDetails(@MappingTarget PayableWriteOffEntity entity) {
        if (entity.getDetails() != null) {
            entity.getDetails().forEach(detail -> detail.setWriteOff(entity));
        }
    }
}
