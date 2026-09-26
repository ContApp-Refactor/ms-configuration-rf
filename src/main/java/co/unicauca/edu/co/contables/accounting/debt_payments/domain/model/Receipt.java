package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.ReceiptType;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.CostCenterUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.PaymentMethodUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.ThirdPartyUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.ResourceUsageNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.ResourceUsageProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Receipt implements ResourceUsageProvider {
    private Long id;
    private String receiptCode;
    private String enterpriseId;
    private Long thirdPartyId;
    private Long paymentMethodId;
    private Long paymentMethodAccount;
    private ReceiptType receiptType;
    private ReceiptStatus status;
    private LocalDate issueDate;
    private Long totalAmount;
    private String observations;
    private String voidReasonDescription;
    private LocalDate voidDate;

    private Long ledgerAccountId;
    private Long centerCostId;
    private List<ReceiptDetail> details;

    /**
     * @brief Check if the receipt is of type invoice payment.
     */
    public boolean isInvoicePayment() {
        return this.receiptType != null && this.receiptType.getId().equals(ReceiptType.INVOICE_PAYMENT.getId());
    }

    /**
     * @brief Method to create a receipt for invoice payment.
     * 
     * @param enterpriseId    ID of the enterprise
     * @param thirdPartyId    ID of the third party
     * @param paymentMethodId ID of the payment method
     * @param observations    Observations for the receipt
     * @param details         List of receipt details
     * @return A new Receipt instance configured for invoice payment
     */
    public static Receipt createForInvoicePayment(String enterpriseId, Long thirdPartyId, Long paymentMethodId,
            String observations, List<ReceiptDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Invoice payment receipt must have at least one detail.");
        }

        Receipt receipt = new Receipt();
        receipt.setReceiptType(ReceiptType.INVOICE_PAYMENT);
        receipt.enterpriseId = enterpriseId;
        receipt.thirdPartyId = thirdPartyId;
        receipt.paymentMethodId = paymentMethodId;
        receipt.observations = observations;
        receipt.details = new ArrayList<>(details);
        receipt.status = ReceiptStatus.FINALIZED;
        receipt.issueDate = LocalDate.now();
        return receipt;
    }

    /**
     * @brief Factory to create a receipt for direct income.
     * 
     * @param enterpriseId    ID of the enterprise
     * @param thirdPartyId    ID of the third party
     * @param paymentMethodId ID of the payment method
     * @param observations    Observations for the receipt
     * @param totalAmount     Total amount of the receipt
     * @param ledgerAccountId Account ledger ID
     * @return A new Receipt instance configured for direct income
     */
    public static Receipt createForDirectIncome(String enterpriseId, Long thirdPartyId, Long paymentMethodId,
            String observations, Long totalAmount, Long ledgerAccountId) {
        if (totalAmount == null || totalAmount <= 0) {
            throw new IllegalArgumentException("Direct income receipt must have a positive total amount.");
        }
        if (ledgerAccountId == null) {
            throw new IllegalArgumentException("Direct income receipt must specify a ledger account.");
        }

        Receipt receipt = new Receipt();
        receipt.setReceiptType(ReceiptType.DIRECT_INCOME);
        receipt.enterpriseId = enterpriseId;
        receipt.thirdPartyId = thirdPartyId;
        receipt.paymentMethodId = paymentMethodId;
        receipt.observations = observations;
        receipt.totalAmount = totalAmount;
        receipt.ledgerAccountId = ledgerAccountId;
        receipt.status = ReceiptStatus.FINALIZED;
        receipt.issueDate = LocalDate.now();

        return receipt;
    }

    /**
     * @brief Apply payments from the receipt details to the corresponding invoices.
     * 
     * @param invoiceFinder A function that knows how to find an invoice by its ID.
     * @return A list of modified invoices that need to be persisted.
     * @throws Exception
     */
    public List<InvoiceReplica> processInvoicePayments(Function<Long, Optional<InvoiceReplica>> invoiceFinder)
            throws Exception {
        if (this.receiptType != ReceiptType.INVOICE_PAYMENT) {
            return Collections.emptyList();
        }

        if(this.details == null || this.details.isEmpty()) {
            throw new IllegalStateException("Invoice payment receipt must have at least one detail.");
        }

        this.calculateTotalFromDetails(); // Asegura que el total sea correcto

        List<InvoiceReplica> modifiedInvoices = new ArrayList<>();
        for (ReceiptDetail detail : this.details) {
            InvoiceReplica invoice = invoiceFinder.apply(detail.getInvoiceId())
                    .orElseThrow(() -> new InvoiceNotFoundException(
                            "Invoice with id " + detail.getInvoiceId() + " not found."));

            if (!invoice.getThirdId().equals(this.thirdPartyId)) {
                throw new IllegalStateException(
                        "Error de inconsistencia: La factura con código " + invoice.getFactCode() +
                                " pertenece al tercero con ID " + invoice.getThirdId() +
                                ", pero el recibo se está creando para el tercero con ID " + this.thirdPartyId + ".");
            }

            // Delegamos la lógica de aplicar el pago a la factura
            invoice.applyPayment(Long.valueOf(detail.getAmountPaid()));

            // Completamos el detalle con info de la factura
            detail.setInvoiceCode(invoice.getFactCode());
            detail.setAccountingAccount(invoice.getAccountingAccount());
            modifiedInvoices.add(invoice);
        }
        return modifiedInvoices;
    }

    /**
     * @brief Void the receipt, reversing its effects on associated invoices if
     *        applicable.
     * 
     * @param reason        The reason for voiding the receipt.
     * @param invoiceFinder A function that knows how to find an invoice by its ID.
     * @return A list of modified invoices that need to be persisted.
     * @throws Exception
     */
    public List<InvoiceReplica> voidReceipt(String reason, Function<Long, Optional<InvoiceReplica>> invoiceFinder)
            throws Exception {
        if (this.status == ReceiptStatus.VOIDED) {
            throw new IllegalStateException("Receipt is already voided.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A reason is required to void a receipt.");
        }
        this.status = ReceiptStatus.VOIDED;
        this.voidReasonDescription = reason;
        this.voidDate = LocalDate.now();

        if (this.receiptType != ReceiptType.INVOICE_PAYMENT) {
            return Collections.emptyList();
        }

        List<InvoiceReplica> modifiedInvoices = new ArrayList<>();
        for (ReceiptDetail detail : this.details) {
            InvoiceReplica invoice = invoiceFinder.apply(detail.getInvoiceId())
                    .orElseThrow(() -> new InvoiceNotFoundException("Associated invoice with id "
                            + detail.getInvoiceId() + " not found. Data might be inconsistent."));

            // Delegamos la lógica de revertir el pago a la factura
            invoice.reversePayment(detail.getAmountPaid());
            modifiedInvoices.add(invoice);
        }
        return modifiedInvoices;
    }

    /**
     * @brief Assign a unique code to the receipt.
     * @param code The unique code to assign.
     */
    public void assignReceiptCode(String code) {
        if (this.receiptCode != null) {
            throw new IllegalStateException("Receipt code is already assigned.");
        }
        this.receiptCode = code;
    }

    /**
     * @brief Calculate the total amount from the receipt details.
     */
    private void calculateTotalFromDetails() {
        if (this.receiptType == ReceiptType.INVOICE_PAYMENT) {
            this.totalAmount = this.details.stream()
                    .mapToLong(ReceiptDetail::getAmountPaid)
                    .sum();
        }
    }

    /**
     * Return a list of resource usage notifications for the resources utilized by
     * this receipt.
     * 
     * @return A list of ResourceUsageNotification instances.
     */
    @Override
    public List<ResourceUsageNotification> getUsageNotifications() {
        List<ResourceUsageNotification> notifications = new ArrayList<>();

        // 1. Tercero
        notifications.add(new ThirdPartyUsedNotification(this.thirdPartyId, this.enterpriseId));

        // 2. Centro de Costo (con su lógica)
        if (this.receiptType == ReceiptType.DIRECT_INCOME && this.centerCostId != null) {
            notifications.add(new CostCenterUsedNotification(this.centerCostId, this.enterpriseId));
        }

        // 3. Método de Pago
        if (this.paymentMethodId != null) {
            notifications.add(new PaymentMethodUsedNotification(this.paymentMethodId, this.enterpriseId));
        }

        // 4. Cuentas Contables principales ya no es necesario

        return notifications;
    }
}