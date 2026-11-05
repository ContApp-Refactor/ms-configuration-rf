package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IInvoiceCommandUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IInvoiceNotificationUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IInvoiceQueryUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.UpdateDueDateRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ApiResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.InvoicePendingResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.IInvoiceRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @brief REST controller for managing invoices, including retrieval and updates 
 * Handles endpoints for fetching invoices by various criteria and updating invoice due dates
 * Also includes an endpoint to trigger invoice payment reminders
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class InvoiceController {
    private final IInvoiceQueryUseCase invoiceQueryUseCase;
    private final IInvoiceCommandUseCase invoiceCommandUseCase;
    private final IInvoiceRestMapper invoiceRestMapper;
    private final IInvoiceNotificationUseCase invoiceNotificationUseCase;

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoicePendingResponse>> getInvoiceById(@PathVariable Long invoiceId) {
        InvoiceReplica invoice = invoiceQueryUseCase.findInvoiceById(invoiceId);
        InvoicePendingResponse responseDto = invoiceRestMapper.toInvoicePendingResponse(invoice);
        
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @GetMapping("/pending/client/{clientId}/enterprise/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<InvoicePendingResponse>>> getPendingInvoicesByClient(@PathVariable Long clientId, @PathVariable String enterpriseId) {
        List<InvoiceReplica> pendingInvoices = invoiceQueryUseCase.findStatusInvoicesByClientId(clientId, InvoiceStatus.PENDING, enterpriseId);

        if (pendingInvoices.isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.successEmpty("No se encontraron facturas pendientes para el cliente.", "NO_CONTENT")
            );
        }

        List<InvoicePendingResponse> responseDtoList = invoiceRestMapper.toInvoicePendingResponseList(pendingInvoices);
        return ResponseEntity.ok(ApiResponse.success(responseDtoList));
    }

    //@PreAuthorize("hasAuthority('Update_Invoice_DueDate')")
    @PatchMapping("/{invoiceId}/due-date")
    public ResponseEntity<ApiResponse<Void>> updateDueDate(@PathVariable Long invoiceId, @Valid @RequestBody UpdateDueDateRequest request) {
        invoiceCommandUseCase.updateDueDate(invoiceId, request.getNewDueDate());

        // <<< 4. RESPUESTA PARA OPERACIONES SIN CONTENIDO (VOID)
        // Usamos ApiResponse.success(null, message) para confirmar la operación.
        return ResponseEntity.ok(ApiResponse.success(null, "La fecha de vencimiento de la factura ha sido actualizada correctamente."));
    }

    @GetMapping("invoices/by-enterprise/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<InvoicePendingResponse>>> getInvoicesByEnterpriseId(@PathVariable String enterpriseId) {
        List<InvoiceReplica> invoices = invoiceQueryUseCase.findInvoicesByEnterpriseId(enterpriseId);

        if (invoices.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.successEmpty("No se encontraron facturas para la empresa.", "NO_CONTENT"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(invoiceRestMapper.toInvoicePendingResponseList(invoices)));
    }

    @GetMapping("invoices/pending/by-enterprise/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<InvoicePendingResponse>>> getPendingInvoicesByEnterpriseId(@PathVariable String enterpriseId) {
        List<InvoiceReplica> pendingInvoices = invoiceQueryUseCase.findPendingInvoicesByEnterpriseId(enterpriseId);

        if (pendingInvoices.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.successEmpty("No se encontraron facturas pendientes para la empresa.", "NO_CONTENT"));
        }

        return ResponseEntity.ok(ApiResponse.success(invoiceRestMapper.toInvoicePendingResponseList(pendingInvoices)));
    }

    @GetMapping("invoices/status/by-client/{clientId}/{status}/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<InvoicePendingResponse>>> getStatusInvoicesByClientId(
            @PathVariable Long clientId,
            @PathVariable InvoiceStatus status,
            @PathVariable String enterpriseId) {
        List<InvoiceReplica> invoices = invoiceQueryUseCase.findStatusInvoicesByClientId(clientId, status, enterpriseId);

        if (invoices.isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.successEmpty("No se encontraron facturas con el estado '" + status + "' para el cliente.", "NO_CONTENT")
            );
        }

        return ResponseEntity.ok(ApiResponse.success(invoiceRestMapper.toInvoicePendingResponseList(invoices)));
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<InvoicePendingResponse>>> getExpiringInvoices(
            @RequestParam String enterpriseId,
            @RequestParam(defaultValue = "5") int days) {
        List<InvoiceReplica> invoices = invoiceQueryUseCase.findExpiringInvoices(enterpriseId, days);

        if (invoices.isEmpty()) {
            return ResponseEntity.ok(
                ApiResponse.successEmpty("No se encontraron facturas por vencer en los próximos " + days + " días.", "NO_CONTENT")
            );
        }

        return ResponseEntity.ok(ApiResponse.success(invoiceRestMapper.toInvoicePendingResponseList(invoices)));
    }

    @PostMapping("/trigger-reminders")
    public ResponseEntity<String> triggerInvoiceReminders() {
        try {
            invoiceNotificationUseCase.processAndPublishDueInvoices(); 
            
            String message = "Proceso de envío de recordatorios iniciado. Revisa los logs de ms_debt_payments y ms_notifications para ver el progreso.";
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            String errorMessage = "Error al iniciar el proceso de recordatorios: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}
