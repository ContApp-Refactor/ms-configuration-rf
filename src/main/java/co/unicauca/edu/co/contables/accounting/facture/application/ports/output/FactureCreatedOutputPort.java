package co.unicauca.edu.co.contables.accounting.facture.application.ports.output;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

public interface FactureCreatedOutputPort {
    Facture saveFacture(Facture facture);
    Long findMaxFactCode();
}
