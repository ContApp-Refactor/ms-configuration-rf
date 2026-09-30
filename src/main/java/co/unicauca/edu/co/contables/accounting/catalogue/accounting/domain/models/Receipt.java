package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.ProcessingStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.SourceDocumentType;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Receipt {
    private Long id; // El ID único de este registro de recibo en la base de datos de contabilidad.
    private Long originalReceiptId; // El ID del recibo en el microservicio de origen (debt_payments). Esencial para
                                    // trazabilidad
    private String receiptCode;
    private String enterpriseId;
    private Long receiptTypeId;
    private Long thirdPartyId;
    private Long paymentMethodId;
    private Long paymentMethodAccount; // Añadido para la cuenta del método de pago
    private String status;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private String observations;
    private Long ledgerAccountId; //se recibe codigo
    private Long centerCostId;
    private List<ReceiptDetail> details;
    private ProcessingStatus processingStatus;

    public AccountingEntry buildAccountingEntryFromReceipt(Function<String, AccountCatalogue> accountFinder,
            String entryCode) {
        List<AccountingMovement> movements = new ArrayList<>();

        // La lógica de búsqueda ahora usa la función que le pasamos
        AccountCatalogue debitAccount = accountFinder.apply(this.paymentMethodAccount.toString());
        if (debitAccount == null) {
            throw new IllegalStateException(
                    "No se encontro la cuenta contable para el código: " + this.paymentMethodAccount);
        }

        movements.add(AccountingMovement.builder()
                .account(debitAccount.getId())
                .thirdPartyId(this.thirdPartyId)
                .description("Ingreso por Recibo de Caja " + this.receiptCode)
                .debit(this.totalAmount)
                .credit(BigDecimal.ZERO)
                .build());

        // La lógica de los tipos de recibo se mantiene, pero usando 'this'
        if (this.receiptTypeId == 1L) { // TIPO: ABONO A FACTURA (Idealmente usar un Enum aquí)
            if (this.details == null || this.details.isEmpty()) {
                throw new IllegalStateException("Recibo tipo 'Abono' (ID 1) no tiene detalles.");
            }
            for (ReceiptDetail detail : this.details) {
                AccountCatalogue creditAccount = accountFinder.apply(detail.getAccountingAccount().toString());
                movements.add(AccountingMovement.builder()
                        .account(creditAccount.getId())
                        .thirdPartyId(this.thirdPartyId)
                        .description("Abono a factura " + detail.getInvoiceCode())
                        .debit(BigDecimal.ZERO)
                        .credit(detail.getAmountPaid())
                        .build());
            }
        } else if (this.receiptTypeId == 2L) { // TIPO: INGRESO DIRECTO
            AccountCatalogue creditAccount = accountFinder.apply(this.ledgerAccountId.toString());
            movements.add(AccountingMovement.builder()
                    .account(creditAccount.getId())
                    .thirdPartyId(this.thirdPartyId)
                    .description("Ingreso directo por Recibo de Caja " + this.receiptCode)
                    .debit(BigDecimal.ZERO)
                    .credit(this.totalAmount)
                    .build());
        } else {
            throw new IllegalStateException("Tipo de recibo desconocido: " + this.receiptTypeId);
        }

        validateDoubleEntry(movements); // Este método también se mueve aquí como privado

        return AccountingEntry.builder()
                .code(entryCode)
                .date(this.issueDate)
                .description("Contabilización de Recibo de Caja " + this.receiptCode)
                .status(AccountingEntryStatus.ACTIVE)
                .sourceDocumentId(this.id) // Importante: usamos el ID del recibo ya guardado
                .type(SourceDocumentType.RECEIPT.name())
                .idEnterprise(this.enterpriseId)
                .centerCostId(this.centerCostId) // Aquí podríamos asignar un centro de costo si es necesario
                .movements(movements)
                .build();
    }

    private void validateDoubleEntry(List<AccountingMovement> movements) {
        BigDecimal totalDebits = movements.stream().map(AccountingMovement::getDebit).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal totalCredits = movements.stream().map(AccountingMovement::getCredit).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new IllegalStateException(
                    "Error de partida doble: la suma de débitos no es igual a la suma de créditos.");
        }
    }
}
