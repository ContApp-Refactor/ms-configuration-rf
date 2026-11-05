package co.unicauca.edu.co.contables.accounting.facture.application.service;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.CreateFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureCreatedOutputPort;
import co.unicauca.edu.co.contables.accounting.facture.domain.event.FactureCreatedEvent;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.eventpublisher.FactureEventPublisherAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateFactureService implements CreateFactureUseCase {

    private final FactureCreatedOutputPort factureCreatedOutputPort;

    private final FactureEventPublisherAdapter factureEventPublisher;

    /**
     * Crea una nueva factura con un código de factura único, la guarda en la base
     * de datos
     * y publica un evento FacturaCreada.
     *
     * @param factura el objeto de factura a crear y guardar
     * @return el objeto de factura guardado con un código de factura asignado
     */
    @Override
    public Facture createFacture(Facture facture) {
        // Generar el siguiente factCode
        Long nextFactCode = generateNextFactCode();
        facture.setFactCode(nextFactCode); // Asignar el nuevo factCode
        System.out.println(facture);
        // Guardar la factura
        facture = factureCreatedOutputPort.saveFacture(facture);

        // Publicar el evento de creación de factura
        factureEventPublisher.publishFactureCreatedEvent(new FactureCreatedEvent(facture.getFactId()));
        
        return facture;
    }

    /**
     * Genera el siguiente factCode a partir del máximo factCode existente en
     * la base de datos.
     *
     * @return el siguiente factCode a asignar a la factura
     */
    private Long generateNextFactCode() {
        // Obtener el factCode máximo desde el repositorio
        Long lastFactCode = factureCreatedOutputPort.findMaxFactCode();
        return (lastFactCode == null) ? 1L : lastFactCode + 1; // Comienza en 1 si no hay factCode
    }

}




