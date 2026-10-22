package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.SupplierInvoiceReplica;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface ISupplierInvoiceProviderPort {
    SupplierInvoiceReplica save(SupplierInvoiceReplica invoice);
    Optional<SupplierInvoiceReplica> findById(Long id);
    Optional<SupplierInvoiceReplica> findLocked(Long id, String enterpriseId);
    Optional<SupplierInvoiceReplica> findBySource(Long sourceId, String enterpriseId);
    List<SupplierInvoiceReplica> findPending(String enterpriseId, Long supplierId);
    List<SupplierInvoiceReplica> findForStatement(String enterpriseId, Long supplierId, LocalDate from,
                                                   LocalDate to, String reference, Boolean active);
}
