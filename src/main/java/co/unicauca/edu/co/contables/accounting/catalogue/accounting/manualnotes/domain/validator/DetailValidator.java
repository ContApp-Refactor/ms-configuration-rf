package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.validator;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto.AccountingNoteDetailRequest;

public interface DetailValidator {

    void validate(AccountingNoteDetailRequest detail, String enterpriseId);
}
