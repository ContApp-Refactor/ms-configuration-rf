package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.application;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.validator.DetailValidator;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteDetailRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegcontableValidationFacade {

    private final List<DetailValidator> validators;

    public RegcontableValidationFacade(List<DetailValidator> validators) {
        this.validators = validators;
    }

    public void validateDetails(List<AccountingNoteDetailRequest> details, String enterpriseId) {
        for (AccountingNoteDetailRequest detail : details) {
            validators.forEach(validator -> validator.validate(detail, enterpriseId));
        }
    }
}
