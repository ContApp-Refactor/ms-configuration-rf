package co.unicauca.edu.co.contables.accounting.facture.application.ports.output;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

public interface FactureGeneratePDFOutputPort {
    byte[] generatePDF(Facture facture);
    byte[] generateQR(Facture facture);
}
