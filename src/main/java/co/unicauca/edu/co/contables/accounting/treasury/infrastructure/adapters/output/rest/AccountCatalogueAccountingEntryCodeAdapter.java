package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.rest;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountingSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountingEntryNotFoundException;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IAccountingEntryCodePort;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resuelve el codigo de un asiento contable contra el modulo catalogue <strong>en el mismo
 * proceso</strong>.
 *
 * <p>Sustituye la auto-llamada HTTP a {@code /api/accountCatalogue/accounting/entries/{id}}, que
 * envolvia la respuesta en {@code ApiResponse{success,data{code}}}. El adaptador anterior tratara
 * como "sin codigo" tanto la ausencia de respuesta como el flag {@code success=false}; aqui el
 * equivalente es que el servicio lance {@link AccountingEntryNotFoundException}, que se traduce en
 * {@code Optional.empty()} igual que antes.
 */
@Component
@Slf4j
public class AccountCatalogueAccountingEntryCodeAdapter implements IAccountingEntryCodePort {

    private final IAccountingSearchInputPort accountingSearchInputPort;

    public AccountCatalogueAccountingEntryCodeAdapter(IAccountingSearchInputPort accountingSearchInputPort) {
        this.accountingSearchInputPort = accountingSearchInputPort;
    }

    @Override
    public Optional<String> resolveCode(Long accountingEntryId) {
        if (accountingEntryId == null) {
            return Optional.empty();
        }
        try {
            AccountingEntry entry = accountingSearchInputPort.findAccountingEntryById(accountingEntryId);
            if (entry == null) {
                return Optional.empty();
            }
            String code = entry.getCode();
            if (code == null || code.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(code);
        } catch (AccountingEntryNotFoundException ex) {
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("No se pudo resolver código de asiento {}: {}", accountingEntryId, ex.getMessage());
            return Optional.empty();
        }
    }
}
