package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * InvoiceReplica class represents a replica of an invoice in the debt payments domain.
 * It contains attributes and methods to manage invoice state and operations.
 */

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class InvoiceReplica {
    private Long id;
    private String factCode; 
    private Long totalValue;
    private Long totalPay;
    private Long thirdId;
    private Long pendingValue;
    private Long accountingAccount;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private String entId;
    private InvoiceStatus status;
    private boolean active;


    /**
     * Method to write off the invoice.
     */
    public void writeOff() {
        if (this.status == InvoiceStatus.PAID) 
            throw new IllegalStateException("Cannot write off a fully paid invoice. FactCode: " + this.factCode);

        if (this.status == InvoiceStatus.WRITTEN_OFF) 
            return;
        
        this.status = InvoiceStatus.WRITTEN_OFF;
        this.pendingValue = 0L;
    }

    /**
     * Method to validate the dates of the invoice.
     */
    public void validateDates() {
        if (this.expirationDate.isBefore(this.creationDate)) {
            throw new IllegalArgumentException("Expiration date cannot be before creation date. FactCode: " + this.factCode);
        }
    }

    /**
     * Method to apply a payment to the invoice.
     * @param amountToPay Amount to be paid in a Receipt
     * @throws Exception if payment cannot be applied
     */
    public void applyPayment(Long amountToPay) {
        if (this.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot apply payment to a fully paid invoice. FactCode: " + this.factCode);
        }
        if (amountToPay <= 0) {
            throw new IllegalStateException("Payment amount must be positive. FactCode: " + this.factCode);
        }
        if (amountToPay > this.pendingValue) {
            throw new IllegalStateException("Amount paid (" + amountToPay + ") exceeds the pending balance (" + this.pendingValue + "). FactCode: " + this.factCode);
        }
        
        this.pendingValue -= amountToPay;
        this.totalPay += amountToPay;
        
        if (this.pendingValue == 0) {
            this.status = InvoiceStatus.PAID;
        } else {
            this.status = InvoiceStatus.PENDING;
        }
    }

    /**
     * Method to reverse a payment applied to the invoice in a Receipt.
     * @param amountToReverse Amount to be reversed
     */
    public void reversePayment(Long amountToReverse) {
        if (amountToReverse <= 0) {
            throw new IllegalArgumentException("Amount to reverse must be positive. FactCode: " + this.factCode);
        }

        this.pendingValue += amountToReverse;
        this.totalPay -= amountToReverse;
        
        if (this.totalPay <= 0) {
            this.totalPay = 0L;
            this.pendingValue = this.totalValue;
            this.status = InvoiceStatus.PENDING; 
        }
    }

    /**
     * Marc the invoice as pending write-off.
     */
    public void markAsPendingWriteOff() {
        // Podrías añadir validaciones aquí si fuera necesario
        this.status = InvoiceStatus.PENDING_WRITTEN_OFF;
    }

    /**
     * Reverse the write-off of the invoice.
     * @param amountToRestore Amount to restore to pending value
     */
    public void reverseWriteOff(Long amountToRestore) {
        if (this.status != InvoiceStatus.WRITTEN_OFF) {
            return; 
        }
        this.status = InvoiceStatus.PENDING;
        this.pendingValue = amountToRestore;
    }
}
