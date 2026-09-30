package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountingSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.exception.AccountingEntryNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AccountingSearchService implements IAccountingSearchInputPort {

    private final IAccountingSearchOutputPort accountingSearchOutputPort;


    @Override
    public AccountingEntry findAccountingEntryById(Long id) {
        return accountingSearchOutputPort.findById(id)
                .orElseThrow(() -> new AccountingEntryNotFoundException("Asiento contable con ID " + id + " no encontrado."));
    }

    @Override
    public AccountingEntry findAccountingEntryByReceiptId(Long receiptId) {
        return accountingSearchOutputPort.findByReceiptId(receiptId)
                .orElseThrow(() -> new AccountingEntryNotFoundException("Asiento contable con ID " + receiptId + " no encontrado."));
    }

    @Override
    public List<AccountingMovement> findMovementsByAccountId(Long accountId) {
        return accountingSearchOutputPort.findMovementsByAccountId(accountId);
    }

    @Override
    public List<AccountingMovement> findMovementsByThirdPartyId(Long thirdPartyId) {
        return accountingSearchOutputPort.findMovementsByThirdPartyId(thirdPartyId);
    }

    @Override
    public AccountingEntry findAccountingEntryBySourceDocumentIdAndType(Long sourceDocumentId, String type) {
        return accountingSearchOutputPort.findBySourceDocumentIdAndType(sourceDocumentId, type)
                .orElseThrow(() -> new AccountingEntryNotFoundException("Asiento contable con documento fuente ID " + sourceDocumentId + " y tipo " + type + " no encontrado."));
    }
}
