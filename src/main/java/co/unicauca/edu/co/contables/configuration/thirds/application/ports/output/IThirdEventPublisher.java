package co.unicauca.edu.co.contables.configuration.thirds.application.ports.output;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;

public interface IThirdEventPublisher {
    public void publishThirdUpdatedEvent(Third third);
}
