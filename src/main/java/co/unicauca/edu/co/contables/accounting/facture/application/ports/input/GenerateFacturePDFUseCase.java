package co.unicauca.edu.co.contables.accounting.facture.application.ports.input;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

public interface GenerateFacturePDFUseCase {
    byte[] generetePDFFacture(Facture facture);
    byte[] generateFactureQR(Facture facture);
}
