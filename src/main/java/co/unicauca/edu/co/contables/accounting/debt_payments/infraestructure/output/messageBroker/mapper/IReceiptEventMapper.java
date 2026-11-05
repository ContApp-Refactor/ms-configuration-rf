package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @brief Mapper interface for converting Receipt domain models to ReceiptEventDto.
 */
@Mapper(componentModel = "spring")
public interface IReceiptEventMapper {
    @Mapping(target = "id", ignore = true)
    ReceiptDetail toEventDtoDetail(ReceiptDetailEventDto detailRequest);

    @Mapping(source = "status", target = "status")
    @Mapping(source = "paymentMethodId", target = "paymentMethodId")
    @Mapping(source = "issueDate", target = "issueDate")
    @Mapping(source = "receiptType.id", target = "receiptTypeId")
    ReceiptEventDto toEventDto(Receipt receipt);

    List<ReceiptEventDto> toEventDtoList(List<Receipt> receipts);

}
