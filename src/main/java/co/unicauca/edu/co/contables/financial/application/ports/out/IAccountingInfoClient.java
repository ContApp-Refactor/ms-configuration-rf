package co.unicauca.edu.co.contables.financial.application.ports.out;

import co.unicauca.edu.co.contables.financial.domain.models.external.accountingInfo.AccountingEntry;

import java.time.LocalDate;
import java.util.List;

public interface IAccountingInfoClient {

    List<AccountingEntry> findAccountingEntries(String entId, LocalDate startDate, LocalDate endDate);
    
}