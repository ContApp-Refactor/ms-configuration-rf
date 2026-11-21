package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.IWeightedAverageEventPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAveragePurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAverageSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

import lombok.extern.slf4j.Slf4j;

@Component("weightedAverageEventPublisher")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class SpringEventsWeightedAverageEventPublisher implements IWeightedAverageEventPort {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsWeightedAverageEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishPurchaseWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        log.info("Publishing weighted average event purchase: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new WeightedAveragePurchaseEvent(kardexDtoRequest, EventFactureType.PURCHASE));
    }

    @Override
    public void publishSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing weighted average event sale: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new WeightedAverageSalesEvent(kardexDtoRequest, EventFactureType.SALE));
    }

    @Override
    public void publishReturnOnSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing weighted average event return on sale: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new WeightedAverageSalesEvent(kardexDtoRequest, EventFactureType.RETURNONSALE));
    }

    @Override
    public void publishReturnOnPurchaseWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing weighted average event return on purchase: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new WeightedAverageSalesEvent(kardexDtoRequest, EventFactureType.RETURNONPURCHASE));
    }

    @Override
    public void publishNonCommercialExitWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        log.info("Publishing weighted average event non commercial exit: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new WeightedAverageSalesEvent(kardexDtoRequest, EventFactureType.NONCOMMERCIALEXIT));
    }

    @Override
    public void publishNonCommercialEntryWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        log.info("Publishing weighted average event non commercial entry: {}", kardexDtoRequest.getFactCode());
        eventPublisher.publishEvent(new WeightedAveragePurchaseEvent(kardexDtoRequest, EventFactureType.NONCOMMERCIALENTRY));
    }
}
