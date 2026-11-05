package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptDetailEventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptEventDTO;

import jakarta.inject.Named;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IReceiptEventInboundMapper {
    /*@Mapping(source= "id", target = "originalReceiptId")
    @Mapping(target = "processingStatus", constant = "RECEIVED")
    Receipt toDomain(ReceiptEventDTO eventDTO);*/

    Receipt toDomain(ReceiptEventDTO receiptEventDTO);

    @Named("mapDetails")
    List<ReceiptDetail> mapDetails(List<ReceiptDetailEventDTO> detailEventDTOs);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "invoiceId", target = "originalInvoiceId")
    ReceiptDetail toDomain(ReceiptDetailEventDTO detailEventDTO);

}
