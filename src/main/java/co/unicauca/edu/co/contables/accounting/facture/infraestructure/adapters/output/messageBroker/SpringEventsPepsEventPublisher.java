package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.IpepsEventPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsPurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

import lombok.extern.slf4j.Slf4j;

@Component("pepsEventPublisher")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class SpringEventsPepsEventPublisher implements IpepsEventPort {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsPepsEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishPurchasePEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        log.info("Publishing peps event purchase: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new PepsPurchaseEvent(kardexDtoRequest, EventFactureType.PURCHASE));
    }

    @Override
    public void publishSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing peps event sale: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new PepsSalesEvent(kardexDtoRequest, EventFactureType.SALE));
    }

    @Override
    public void publishReturnOnSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing peps event return on sale: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new PepsSalesEvent(kardexDtoRequest, EventFactureType.RETURNONSALE));
    }

    @Override
    public void publishReturnOnPurchasePEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing peps event return on purchase: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new PepsSalesEvent(kardexDtoRequest, EventFactureType.RETURNONPURCHASE));
    }

    @Override
    public void publishNonCommercialExitPEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing peps event non commercial exit: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new PepsSalesEvent(kardexDtoRequest, EventFactureType.NONCOMMERCIALEXIT));
    }

    @Override
    public void publishNonCommercialEntryPEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        log.info("Publishing peps event non commercial entry: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new PepsPurchaseEvent(kardexDtoRequest, EventFactureType.NONCOMMERCIALENTRY));
    }
}
