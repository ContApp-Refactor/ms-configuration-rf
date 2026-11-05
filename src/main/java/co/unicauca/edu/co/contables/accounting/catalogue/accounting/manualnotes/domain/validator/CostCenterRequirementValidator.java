package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.validator;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.AccountPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.CostCenterPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteDetailRequest;
import org.springframework.stereotype.Component;

@Component
public class CostCenterRequirementValidator implements DetailValidator {

    private final AccountPort accountPort;
    private final CostCenterPort costCenterPort;

    public CostCenterRequirementValidator(AccountPort accountPort, CostCenterPort costCenterPort) {
        this.accountPort = accountPort;
        this.costCenterPort = costCenterPort;
    }

    @Override
    public void validate(AccountingNoteDetailRequest detail, String enterpriseId) {
        accountPort.findByCode(detail.accountCode(), enterpriseId).ifPresent(account -> {
            if (account.requiresCostCenter()) {
                if (detail.costCenterCode() == null || detail.costCenterCode().isBlank()) {
                    throw new IllegalArgumentException(
                            "La cuenta " + detail.accountCode() + " requiere un Centro de Costos.");
                }
                if (!costCenterPort.existsAndIsActive(detail.costCenterCode(), enterpriseId)) {
                    throw new IllegalArgumentException(
                            "El Centro de Costos " + detail.costCenterCode() + " no existe o está inactivo.");
                }
            }
        });
    }
}
