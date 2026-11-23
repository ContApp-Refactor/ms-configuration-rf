package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.PortfolioWriteOffEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @brief Mapper interface for converting PortfolioWriteOff domain models to PortfolioWriteOffEventDto.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IPortfolioWriteOffEventMapper {

    /**
     * Map a object of PortfolioWriteOff to PortfolioWriteOffEventDto
     * @param domain the PortfolioWriteOff domain object
     * @return the mapped PortfolioWriteOffEventDto object
     */
    @Mapping(target = "details", ignore = true)
    @Mapping(source = "costCenterId", target = "centerCostId")
    PortfolioWriteOffEventDto toEventDto(PortfolioWriteOff domain);
}
