package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models;

import java.time.LocalDate;
import java.util.List;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountingEntry {
    private Long id;        // ID del asiento en la base de datos de contabilidad. 
    private String code;        // Código único del asiento contable (ej. "AE-2024-001"). 
    private LocalDate date;        // Fecha del asiento contable, usualmente la fecha del documento origen.
    private String description;        // Descripción general del asiento. Ej: "Contabilización de Recibo de Caja RC-12345".
    private AccountingEntryStatus status;        // Estado del asiento, ej: ACTIVE, VOIDED.
    private Long sourceDocumentId;        // ID del documento que originó este asiento (el ID de nuestro modelo Receipt).
    private String type; // Tipo del documento que originó este asiento (ej. RECEIPT, PORTFOLIO_WRITEOFF).
    private List<AccountingMovement> movements;        // La lista de movimientos (débitos y créditos) que componen este asiento.
    private String idEnterprise; // ID de la empresa a la que pertenece el asiento contable.
    private Long centerCostId; // ID del centro de costo asociado al asiento contable.

public void voidEntry() {
    if (this.status == AccountingEntryStatus.VOIDED) {
        // Idempotencia: si ya está anulado, no hacer nada.
        return;
    }
    if (this.status != AccountingEntryStatus.ACTIVE) {
        throw new IllegalStateException("Solo se puede anular un asiento en estado ACTIVE. Estado actual: " + this.status);
    }
    this.status = AccountingEntryStatus.VOIDED;
}
}
