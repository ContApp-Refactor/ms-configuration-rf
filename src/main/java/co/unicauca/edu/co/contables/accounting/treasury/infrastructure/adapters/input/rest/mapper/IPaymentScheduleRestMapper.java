package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentSchedule;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.Schedule;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.ScheduleRequest;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.ScheduleResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IPaymentScheduleRestMapper {
    Schedule toCommand(ScheduleRequest request);

    @Mapping(target = "voucherNumber", ignore = true)
    ScheduleResponse toResponse(PaymentSchedule schedule);

    List<ScheduleResponse> toResponseList(List<PaymentSchedule> schedules);
}
