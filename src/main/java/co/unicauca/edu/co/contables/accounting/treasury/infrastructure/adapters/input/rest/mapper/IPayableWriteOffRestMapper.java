package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOffDetail;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.WriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.WriteOffDetailResponse;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.WriteOffRequest;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.WriteOffResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IPayableWriteOffRestMapper {
    WriteOff toCommand(WriteOffRequest request);

    @Mapping(target = "accountingEntryCode", ignore = true)
    WriteOffResponse toResponse(PayableWriteOff writeOff);

    List<WriteOffResponse> toResponseList(List<PayableWriteOff> writeOffs);

    @Mapping(target = "invoiceReference", ignore = true)
    @Mapping(target = "originalAmount", ignore = true)
    @Mapping(target = "availableAmount", ignore = true)
    WriteOffDetailResponse toDetailResponse(PayableWriteOffDetail detail);
}
