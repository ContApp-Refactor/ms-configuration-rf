package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.Voucher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.VoucherRequest;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rest.dto.TreasuryDtos.VoucherResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IPaymentVoucherRestMapper {
    Voucher toCommand(VoucherRequest request);
    @Mapping(target = "accountingEntryCode", ignore = true)
    VoucherResponse toResponse(PaymentVoucher voucher);
    List<VoucherResponse> toResponseList(List<PaymentVoucher> vouchers);
}
