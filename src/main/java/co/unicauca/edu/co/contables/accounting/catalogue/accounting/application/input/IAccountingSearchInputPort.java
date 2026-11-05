package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input;

import java.util.List;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;

public interface IAccountingSearchInputPort {

    AccountingEntry findAccountingEntryById(Long id);
    
    AccountingEntry findAccountingEntryByReceiptId(Long receiptId);
    
    // TODO:  Metodo para buscar un asiento contable por ID de documento fuente y tipo
    AccountingEntry findAccountingEntryBySourceDocumentIdAndType(Long sourceDocumentId, String type);

    List<AccountingMovement> findMovementsByAccountId(Long accountId);

    List<AccountingMovement> findMovementsByThirdPartyId(Long thirdPartyId);
    
} 
