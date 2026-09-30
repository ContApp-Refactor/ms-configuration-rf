package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IPortfolioSearchInputPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ApiResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.ClientPortfolioSummaryResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.InvoiceDetailResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.PortfolioAgingAccountResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.ReceiptSummaryResponse;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/accountCatalogue/portfolio")
@AllArgsConstructor
public class PortfolioController {
    private final IPortfolioSearchInputPort portfolioSearchInputPort;

    @GetMapping("/clients-summary")
    public ResponseEntity<ApiResponse<List<ClientPortfolioSummaryResponse>>> getClientsSummary(
            @RequestParam List<Long> clientIds) {
        List<ClientPortfolioSummaryResponse> summary = portfolioSearchInputPort.getClientPortfolioSummary(clientIds);

        if (summary.isEmpty()) {
            return ResponseEntity.ok(ApiResponse
                    .successEmpty("No se encontró resumen de cartera para los clientes proporcionados.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/invoices/by-client/{clientId}")

    public ResponseEntity<ApiResponse<List<InvoiceDetailResponse>>> getInvoicesByClient(@PathVariable Long clientId) {
        List<InvoiceDetailResponse> invoices = portfolioSearchInputPort.getInvoiceDetailsByClientId(clientId);

        if (invoices.isEmpty()) {
            return ResponseEntity
                    .ok(ApiResponse.successEmpty("No se encontraron facturas para el cliente.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/receipts/by-invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<ReceiptSummaryResponse>>> getReceiptsByInvoice(
            @PathVariable Long invoiceId) {
        List<ReceiptSummaryResponse> receipts = portfolioSearchInputPort.findReceiptsByInvoiceId(invoiceId);

        if (receipts.isEmpty()) {
            return ResponseEntity
                    .ok(ApiResponse.successEmpty("No se encontraron recibos asociados a la factura.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(receipts));
    }

    @GetMapping("/aging-report")
    public ResponseEntity<ApiResponse<List<PortfolioAgingAccountResponse>>> getPortfolioAgingReport(
            @RequestParam(required = false) Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate,
            @RequestParam String enterpriseId,
            @RequestParam(required = false, defaultValue = "false") boolean includeDocuments) {

        List<PortfolioAgingAccountResponse> report = portfolioSearchInputPort.getPortfolioAgingReport(clientId,
                cutoffDate, enterpriseId, includeDocuments);

        if (report.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.successEmpty(
                    "No se generaron datos para el reporte de edades con los filtros proporcionados.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
