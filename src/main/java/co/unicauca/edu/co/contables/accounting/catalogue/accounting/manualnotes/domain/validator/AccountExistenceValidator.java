package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.validator;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.AccountPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteDetailRequest;
import org.springframework.stereotype.Component;

@Component
public class AccountExistenceValidator implements DetailValidator {

    private final AccountPort accountPort;

    public AccountExistenceValidator(AccountPort accountPort) {
        this.accountPort = accountPort;
    }

    @Override
    public void validate(AccountingNoteDetailRequest detail, String enterpriseId) {
        AccountPort.Account account = accountPort.findByCode(detail.accountCode(), enterpriseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La cuenta contable " + detail.accountCode() + " no existe."));

        if (!account.auxiliary()) {
            throw new IllegalArgumentException(
                    "La cuenta " + detail.accountCode() + " no es de nivel auxiliar y no puede recibir movimientos.");
        }
    }
}
