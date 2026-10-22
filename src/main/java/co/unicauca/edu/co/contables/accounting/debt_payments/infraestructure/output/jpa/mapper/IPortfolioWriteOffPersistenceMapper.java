package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.PortfolioWriteOffEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.WriteOffDetailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting between Portfolio Write-Off domain models and Portfolio Write-Off JPA entities.
 * Utilizes MapStruct for automatic generation of mapping implementations.
 */

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IPortfolioWriteOffPersistenceMapper {

    PortfolioWriteOffEntity toEntity(PortfolioWriteOff domain);
    PortfolioWriteOff toDomain(PortfolioWriteOffEntity entity);

    WriteOffDetailEntity toEntity(WriteOffDetail domain);
    WriteOffDetail toDomain(WriteOffDetailEntity entity);

    List<PortfolioWriteOff> toDomainList(List<PortfolioWriteOffEntity> entityList);
}
