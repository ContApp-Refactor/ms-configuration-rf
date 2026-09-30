package co.unicauca.edu.co.contables.accounting.facture.application.service;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.GenerateFacturePDFUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureGeneratePDFOutputPort;
import co.unicauca.edu.co.contables.accounting.facture.domain.event.FacturePDFGeneratedEvent;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.eventpublisher.FactureEventPublisherAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GeneratedPDFFactureService implements GenerateFacturePDFUseCase {

    private final FactureGeneratePDFOutputPort factureGeneratePDFOutputPort;
    private final FactureEventPublisherAdapter factureEventPublisher;

    /**
     * Genera un PDF para la factura dada y publica un evento que indica
     * que el PDF ha sido generado.
     *
     * @param factura La factura para la cual se va a generar el PDF.
     * @return Un arreglo de bytes que representa el PDF generado.
     */
    @Override
    public byte[] generetePDFFacture(Facture facture) {
        byte[] result = factureGeneratePDFOutputPort.generatePDF(facture);
        factureEventPublisher.publishFactureGeneratePDFEvent(new FacturePDFGeneratedEvent(facture.getFactId()));
        return result;
    }

    /**
     * Genera un codigo QR para la factura dada. La responsabilidad de
     * guardar el codigo QR es del puerto de salida.
     *
     * @param facture La factura para la que se va a generar el c digo QR.
     * @return Un arreglo de bytes que representa el c digo QR generado.
     */
    @Override
    public byte[] generateFactureQR(Facture facture) {
        byte[] result = factureGeneratePDFOutputPort.generateQR(facture);
        return result;
    }

}
