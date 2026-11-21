package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service("purchaseInvoiceOutboxService")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SpringEventsPurchaseInvoiceOutboxService extends PurchaseInvoiceOutboxService {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsPurchaseInvoiceOutboxService(ObjectMapper mapper,
            PayableAccountCodeResolver payableAccountCodeResolver,
            ApplicationEventPublisher eventPublisher) {
        super(mapper, payableAccountCodeResolver);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void enqueue(SkeletonFacture facture, String eventType) {
        try {
            PurchaseInvoiceEventDto event = buildEvent(facture);
            event.setEventId(UUID.randomUUID().toString());
            event.setEventType(eventType);
            eventPublisher.publishEvent(event);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible publicar el evento de compra", ex);
        }
    }
}
