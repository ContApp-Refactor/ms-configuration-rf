package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output;

import java.util.Optional;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;

public interface IAccountingEntryPersistenceOutputPort {
    AccountingEntry save(AccountingEntry accountingEntry);

    Optional<AccountingEntry> findBySourceDocumentId(Long sourceDocumentId);
}
