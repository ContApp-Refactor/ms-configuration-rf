package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input;

import java.time.LocalDate;
import java.util.List;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.ClientPortfolioSummaryResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.ReceiptSummaryResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.InvoiceDetailResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.PortfolioAgingAccountResponse;

public interface IPortfolioSearchInputPort {
    List<ClientPortfolioSummaryResponse> getClientPortfolioSummary(List<Long> clientIds);
    List<InvoiceReplica> findPendingInvoicesByClientId(Long thirdIds);
    List<ReceiptSummaryResponse> findReceiptsByInvoiceId(Long invoiceId);
    List<InvoiceDetailResponse> getInvoiceDetailsByClientId(Long clientId);
    List<PortfolioAgingAccountResponse> getPortfolioAgingReport(Long clientId, LocalDate cutoffDate, String enterpriseId, boolean includeDocuments);
}
