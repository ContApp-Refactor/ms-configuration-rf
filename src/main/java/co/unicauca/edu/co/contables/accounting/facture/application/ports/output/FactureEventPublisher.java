package co.unicauca.edu.co.contables.accounting.facture.application.ports.output;

import co.unicauca.edu.co.contables.accounting.facture.domain.event.FactureCreatedEvent;
import co.unicauca.edu.co.contables.accounting.facture.domain.event.FacturePDFGeneratedEvent;

public interface FactureEventPublisher {
    void publishFactureCreatedEvent(FactureCreatedEvent event);
    void publishFactureGeneratePDFEvent(FacturePDFGeneratedEvent event);
}
