package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.validator;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.AccountPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.ThirdPartyPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteDetailRequest;
import org.springframework.stereotype.Component;

@Component
public class ThirdPartyRequirementValidator implements DetailValidator {

    private final AccountPort accountPort;
    private final ThirdPartyPort thirdPartyPort;

    public ThirdPartyRequirementValidator(AccountPort accountPort, ThirdPartyPort thirdPartyPort) {
        this.accountPort = accountPort;
        this.thirdPartyPort = thirdPartyPort;
    }

    @Override
    public void validate(AccountingNoteDetailRequest detail, String enterpriseId) {
        accountPort.findByCode(detail.accountCode(), enterpriseId).ifPresent(account -> {
            if (account.requiresThirdParty()) {
                if (detail.thirdPartyDocument() == null || detail.thirdPartyDocument().isBlank()) {
                    throw new IllegalArgumentException(
                            "La cuenta " + detail.accountCode() + " requiere un tercero asociado.");
                }
                if (!thirdPartyPort.existsAndIsActive(detail.thirdPartyDocument(), enterpriseId)) {
                    throw new IllegalArgumentException(
                            "El tercero con documento " + detail.thirdPartyDocument() + " no existe o está inactivo.");
                }
            }
        });
    }
}
