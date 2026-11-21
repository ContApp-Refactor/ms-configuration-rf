package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@Component("receiptPublisher")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SpringEventsReceiptPublisher extends ReceiptPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsReceiptPublisher(RabbitTemplate rabbitTemplate, IJwtUtils jwtUtils,
            ApplicationEventPublisher eventPublisher) {
        super(rabbitTemplate, jwtUtils);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishSaleReceiptEvent(ReceiptSalesDtoRequest receiptSalesDtoRequest) {
        eventPublisher.publishEvent(new InvoiceSaleEvent(receiptSalesDtoRequest));
    }

    @Override
    public void publishPurchaseInvoiceEvent(PurchaseInvoiceEventDto event) {
        eventPublisher.publishEvent(event);
    }
}
