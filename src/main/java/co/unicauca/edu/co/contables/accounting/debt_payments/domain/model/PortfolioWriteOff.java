package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.CostCenterUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.ThirdPartyUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.ResourceUsageNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.ResourceUsageProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class PortfolioWriteOff implements ResourceUsageProvider{
    private Long id;
    private String code;
    private String justification;
    private Long totalAmount;
    private LocalDate writeOffDate;
    private Long debitAuxiliaryAccount;
    private Long debitAuxiliaryAccountId;
    private Long thirdId;
    private Long costCenterId;
    private WriteOffStatus status;
    private String enterpriseId;
    private List<WriteOffDetail> details;

    /**
     * @brief Factory method to create a PortfolioWriteOff instance.
     * @param enterpriseId The ID of the enterprise associated with the write-off.
     * @param thirdId The ID of the third party associated with the write-off.
     * @param justification The justification for the write-off.
     * @param details The list of write-off details (invoices).
     * @param costCenterId The ID of the cost center associated with the write-off.
     * @return A new instance of PortfolioWriteOff initialized with the provided parameters.
     */
    public static PortfolioWriteOff create(String enterpriseId, Long thirdId, String justification,
            List<WriteOffDetail> details, Long costCenterId) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("A write-off must have at least one invoice detail.");
        }
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("A justification is required for a write-off.");
        }

        PortfolioWriteOff writeOff = new PortfolioWriteOff();
        writeOff.enterpriseId = enterpriseId;
        writeOff.thirdId = thirdId;
        writeOff.justification = justification;
        writeOff.details = details;
        writeOff.status = WriteOffStatus.PENDING_CONFIRMATION; 
        writeOff.writeOffDate = LocalDate.now();
        writeOff.costCenterId = costCenterId;

        writeOff.calculateTotalAmount();

        return writeOff;
    }

    /**
     * @brief Calculate the total amount of the write-off based on its details.
     */
    private void calculateTotalAmount() {
        if (this.details != null) {
            this.totalAmount = this.details.stream()
                    .mapToLong(WriteOffDetail::getAmountWrittenOff)
                    .sum();
        }
    }

    /**
     * @brief Confirm the write-off. Changes the status and validates the transition.
     */
    public void confirm() {
        if (this.status != WriteOffStatus.PENDING_CONFIRMATION) {
            throw new IllegalStateException("Only a write-off with PENDING_CONFIRMATION status can be confirmed.");
        }
        this.status = WriteOffStatus.CONFIRMED;
    }

    /**
     * @brief Void the confirmation of the write-off. Changes the status and validates the transition.
     */
    public void voidConfirmation() {
        if (this.status != WriteOffStatus.CONFIRMED) {
            throw new IllegalStateException("Only a CONFIRMED write-off can be voided.");
        }
        this.status = WriteOffStatus.VOIDED;
    }

    /**
     * @brief Prepare invoices for write-off by marking them as pending.
     * @return List of modified invoices to be persisted.
     */
    public List<InvoiceReplica> prepareInvoicesForWriteOff(Function<Long, Optional<InvoiceReplica>> invoiceFinder) {
        List<InvoiceReplica> modifiedInvoices = new ArrayList<>();
        for (WriteOffDetail detail : this.details) {
            InvoiceReplica invoice = invoiceFinder.apply(detail.getInvoiceId())
                    .orElseThrow(() -> new InvoiceNotFoundException("Invoice " + detail.getInvoiceId() + " not found."));
            
            // Aquí el dominio se auto-completa
            detail.setAmountWrittenOff(invoice.getPendingValue());
            detail.setAccountingAccount(invoice.getAccountingAccount());
            
            invoice.markAsPendingWriteOff(); // Nuevo método en InvoiceReplica
            modifiedInvoices.add(invoice);
        }
        this.calculateTotalAmount(); // Recalcular total con los valores reales
        return modifiedInvoices;
    }

    /**
     * @brief Execute the final write-off on the invoices.
     * @return List of modified invoices to be persisted.
     */
    public List<InvoiceReplica> processConfirmation(Function<Long, Optional<InvoiceReplica>> invoiceFinder) {
        if (this.status != WriteOffStatus.CONFIRMED) {
            throw new IllegalStateException("Write-off must be CONFIRMED to process.");
        }
        List<InvoiceReplica> modifiedInvoices = new ArrayList<>();
        for (WriteOffDetail detail : this.details) {
            InvoiceReplica invoice = invoiceFinder.apply(detail.getInvoiceId())
                    .orElseThrow(() -> new InvoiceNotFoundException("Invoice " + detail.getInvoiceId() + " not found."));
            invoice.writeOff();
            modifiedInvoices.add(invoice);
        }
        return modifiedInvoices;
    }

    /**
     * @brief Reverse the write-off on the invoices.
     * @return List of modified invoices to be persisted.
     */
    public List<InvoiceReplica> processVoidance(Function<Long, Optional<InvoiceReplica>> invoiceFinder) {
        if (this.status != WriteOffStatus.VOIDED) {
            throw new IllegalStateException("Write-off must be VOIDED to process voidance.");
        }
        List<InvoiceReplica> modifiedInvoices = new ArrayList<>();
        for (WriteOffDetail detail : this.details) {
            InvoiceReplica invoice = invoiceFinder.apply(detail.getInvoiceId())
                    .orElseThrow(() -> new InvoiceNotFoundException("Invoice " + detail.getInvoiceId() + " not found."));
            invoice.reverseWriteOff(detail.getAmountWrittenOff()); // Nuevo método en InvoiceReplica
            modifiedInvoices.add(invoice);
        }
        return modifiedInvoices;
    }

    /**
     * @brief Get the list of resource usage notifications for this write-off.
     * @return List of ResourceUsageNotification instances representing the resources used by this write-off.
     */
    @Override
    public List<ResourceUsageNotification> getUsageNotifications() {
        List<ResourceUsageNotification> notifications = new ArrayList<>();

        // 1. Tercero (siempre se usa)
        notifications.add(new ThirdPartyUsedNotification(this.thirdId, this.enterpriseId));

        // 2. Centro de Costo (solo si se especifica)
        if (this.costCenterId != null) {
            notifications.add(new CostCenterUsedNotification(this.costCenterId, this.enterpriseId));
        }

        return notifications.stream().distinct().collect(Collectors.toList());
    }
}
