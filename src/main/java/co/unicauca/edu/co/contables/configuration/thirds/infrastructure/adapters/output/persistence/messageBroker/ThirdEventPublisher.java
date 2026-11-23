package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.rabbitConfig.RabbitThirdsEventsConfig;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThirdEventPublisher implements IThirdEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final IJwtUtils jwtUtils;

    @Override
    public void publishThirdUpdatedEvent(Third third) {
        ThirdUpdatedEventDto eventDto = ThirdUpdatedEventDto.builder()
                .thirdId(third.getThId())
                .email(third.getEmail())
                .state(third.getState())
                .entId(third.getEntId())
                .build();


        //Logica para ver si se almacena el nombre o razon social
        if (third.getPersonType().isNatural()) {
            eventDto.setFullName(third.getNames() + " " + third.getLastNames());
        } else {
            eventDto.setFullName(third.getSocialReason());
        }
        
        EventDto<ThirdUpdatedEventDto, String> event = new EventDto<>(eventDto, "THIRD_UPDATED");

        log.info("Publishing third updated event: {}", eventDto.getThirdId());

        rabbitTemplate.convertAndSend(RabbitThirdsEventsConfig.THIRD_UPDATED_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    
}
