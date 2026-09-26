package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.ReceiptCreateRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.ReceiptDetailRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ReceiptResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting Receipt domain models to REST request and response DTOs.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ReceiptTypeMapper.class}) 
public interface IReceiptRestMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receiptCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    @Mapping(target = "voidReasonDescription", ignore = true)
    @Mapping(target = "voidDate", ignore = true)
    @Mapping(source = "receiptTypeId", target = "receiptType")
    Receipt toDomain(ReceiptCreateRequest request);

    @Mapping(target = "id", ignore = true)
    ReceiptDetail toDomain(ReceiptDetailRequest detailRequest);

    @Mapping(source = "status", target = "status")
    @Mapping(source = "paymentMethodId", target = "paymentMethodId")
    @Mapping(source = "issueDate", target = "issueDate")
    @Mapping(source = "receiptType.id", target = "receiptTypeId")
    ReceiptResponse toResponse(Receipt receipt);

    List<ReceiptResponse> toResponseList(List<Receipt> receipts);
}
