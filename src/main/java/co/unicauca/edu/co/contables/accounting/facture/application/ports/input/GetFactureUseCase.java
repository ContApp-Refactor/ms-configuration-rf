package co.unicauca.edu.co.contables.accounting.facture.application.ports.input;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

public interface GetFactureUseCase {
    Facture getFactureBy(Long factId);
}
