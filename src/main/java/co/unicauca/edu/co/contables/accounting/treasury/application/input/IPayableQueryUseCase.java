package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.SupplierInvoiceReplica;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AgingLine;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.SupplierStatement;
import java.time.LocalDate;
import java.util.List;

public interface IPayableQueryUseCase {
    List<SupplierInvoiceReplica> pending(String enterpriseId, Long supplierId);
    SupplierInvoiceReplica find(Long id, String enterpriseId);
    SupplierStatement statement(String enterpriseId, Long supplierId, LocalDate from, LocalDate to,
                                String invoiceReference, Boolean active);
    List<AgingLine> aging(String enterpriseId, LocalDate cutoff, Long supplierId, String accountCode,
                          String document);
}
