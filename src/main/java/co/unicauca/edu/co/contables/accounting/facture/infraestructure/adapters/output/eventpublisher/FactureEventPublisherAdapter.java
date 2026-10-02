package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.eventpublisher;

import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.domain.event.FactureCreatedEvent;
import co.unicauca.edu.co.contables.accounting.facture.domain.event.FacturePDFGeneratedEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FactureEventPublisherAdapter implements FactureEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publica un evento FacturaCreada a todos los oyentes interesados.
     * 
     * @param evento el evento a publicar
     */
    @Override
    public void publishFactureCreatedEvent(FactureCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento FacturaPDFGenerada a todos los oyentes interesados.
     * 
     * @param evento el evento a publicar
     */
    @Override
    public void publishFactureGeneratePDFEvent(FacturePDFGeneratedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
