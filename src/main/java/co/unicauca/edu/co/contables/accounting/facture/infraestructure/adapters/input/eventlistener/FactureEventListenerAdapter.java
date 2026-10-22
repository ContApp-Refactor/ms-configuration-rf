package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.eventlistener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.domain.event.FactureCreatedEvent;
import co.unicauca.edu.co.contables.accounting.facture.domain.event.FacturePDFGeneratedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FactureEventListenerAdapter {

    /**
     * Maneja un evento FacturaCreada registrándolo.
     * 
     * @param evento el evento a manejar
     */
    @EventListener
    public void handleCreate(FactureCreatedEvent event) {
        log.info("Facture created with id " + event.getFactId() + " at " + event.getDate());
    }

    /**
     * Maneja un evento FacturaPDFGenerada registr ndolo.
     * 
     * @param evento el evento a manejar
     */
    @EventListener
    public void handleGeneratePDF(FacturePDFGeneratedEvent event) {
        log.info("PDF generated of facture with id " + event.getFactId() + " at " + event.getDate());
    }
}
