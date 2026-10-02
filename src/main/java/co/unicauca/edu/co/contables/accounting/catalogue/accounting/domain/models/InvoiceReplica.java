package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReplica {
    private Long id;
    private String factCode;
    private BigDecimal totalValue;
    private BigDecimal totalPay;
    private Long thirdId;
    private BigDecimal pendingValue;
    private Long accountingAccount;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private String entId;
    private InvoiceStatus status;
    private boolean active;

    /**
     * Lógica de negocio para castigar la cartera.
     */
    public void writeOff() {
        if (this.status == InvoiceStatus.PAID)
            throw new IllegalStateException("Cannot write off a fully paid invoice. FactCode: " + this.factCode);

        if (this.status == InvoiceStatus.WRITTEN_OFF)
            return;

        this.status = InvoiceStatus.WRITTEN_OFF;
        this.pendingValue = BigDecimal.ZERO;
    }

    /**
     * Metodo para validar que la fecha de vencimiento no sea anterior a la fecha de
     * creacion.
     */
    public void validateDates() {
        if (this.expirationDate.isBefore(this.creationDate)) {
            throw new IllegalArgumentException(
                    "Expiration date cannot be before creation date. FactCode: " + this.factCode);
        }
    }

    public void applyPayment(BigDecimal amountToPay) {
        if (amountToPay.compareTo(this.pendingValue) > 0) {
            throw new IllegalArgumentException("El monto a pagar excede el saldo pendiente.");
        }
        this.pendingValue = this.pendingValue.subtract(amountToPay);
        this.totalPay = this.totalPay.add(amountToPay);

        if (this.pendingValue.compareTo(BigDecimal.ZERO) == 0) {
            this.status = InvoiceStatus.PAID;
        }
    }

    public void reversePayment(BigDecimal amountToReverse) {
        this.pendingValue = this.pendingValue.add(amountToReverse);
        this.totalPay = this.totalPay.subtract(amountToReverse);
        // Lógica para cambiar el estado si es necesario
        if (this.status == InvoiceStatus.PAID) {
            this.status = InvoiceStatus.PENDING; // O el estado que corresponda
        }
    }

     /**
     * Reversa el estado de castigo de una factura, restaurando su saldo pendiente.
     * @param amountToRestore El valor exacto que fue castigado originalmente.
     */
    public void reverseWriteOff(BigDecimal amountToRestore) {
        if (this.status != InvoiceStatus.WRITTEN_OFF) {
            throw new IllegalStateException("Solo se puede revertir el castigo de una factura en estado WRITTEN_OFF. FactCode: " + this.factCode);
        }
        if (amountToRestore.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto a restaurar no puede ser negativo.");
        }
        
        // El saldo pendiente se restaura al valor que tenía antes del castigo.
        this.pendingValue = amountToRestore; 
        this.status = InvoiceStatus.PENDING; // O el estado que corresponda (ej. VENCIDA)
    }

}
