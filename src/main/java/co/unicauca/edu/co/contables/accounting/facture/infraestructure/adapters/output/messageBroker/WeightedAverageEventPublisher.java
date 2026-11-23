package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.IWeightedAverageEventPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig.RabbitWeightedAverageConfig;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeightedAverageEventPublisher implements IWeightedAverageEventPort {

    private final RabbitTemplate rabbitTemplate;
    private final IJwtUtils jwtUtils;
 
    @Override
    public void publishPurchaseWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        EventDto<KardexPurchaseDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.PURCHASE, kardexDtoRequest);
        log.info("Publishing weighted average event purchase: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.SALE, kardexDtoRequest);
        log.info("Publishing weighted average event sale: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishReturnOnSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.RETURNONSALE, kardexDtoRequest);
        log.info("Publishing weighted average event return on sale: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishReturnOnPurchaseWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.RETURNONPURCHASE, kardexDtoRequest);
        log.info("Publishing weighted average event return on purchase: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishNonCommercialExitWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.NONCOMMERCIALEXIT, kardexDtoRequest);
        log.info("Publishing weighted average event non commercial exit: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishNonCommercialEntryWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        EventDto<KardexPurchaseDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.NONCOMMERCIALENTRY, kardexDtoRequest);
        log.info("Publishing weighted average event non commercial entry: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }


    

}
