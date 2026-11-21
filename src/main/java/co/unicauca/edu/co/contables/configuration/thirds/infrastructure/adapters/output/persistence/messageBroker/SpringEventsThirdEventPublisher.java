package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEvent;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto.ThirdUpdatedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("thirdEventPublisher")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsThirdEventPublisher implements IThirdEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishThirdUpdatedEvent(Third third) {
        ThirdUpdatedEventDto eventDto = ThirdUpdatedEventDto.builder()
                .thirdId(third.getThId())
                .email(third.getEmail())
                .state(third.getState())
                .entId(third.getEntId())
                .build();

        if (third.getPersonType().isNatural()) {
            eventDto.setFullName(third.getNames() + " " + third.getLastNames());
        } else {
            eventDto.setFullName(third.getSocialReason());
        }

        log.info("Publishing third updated event: {}", eventDto.getThirdId());

        eventPublisher.publishEvent(new ThirdUpdatedEvent(eventDto));
    }
}
