package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.InvoicePendingResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.InvoiceSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting InvoiceReplica domain models to REST response DTOs.
 */

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IInvoiceRestMapper {
    @Mapping(source = "id", target = "id")
    InvoicePendingResponse toInvoicePendingResponse(InvoiceReplica invoiceReplica);

    List<InvoicePendingResponse> toInvoicePendingResponseList(List<InvoiceReplica> invoiceReplicaList);

    InvoiceSummaryResponse toSummaryResponse(InvoiceReplica invoiceReplica);

    List<InvoiceSummaryResponse> toSummaryResponseList(List<InvoiceReplica> invoiceReplicaList);
}
