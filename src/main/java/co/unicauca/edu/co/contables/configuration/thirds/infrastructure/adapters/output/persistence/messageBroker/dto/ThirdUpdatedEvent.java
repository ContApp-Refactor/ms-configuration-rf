package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto;

public class ThirdUpdatedEvent extends EventDto<ThirdUpdatedEventDto, String> {

    public static final String TYPE = "THIRD_UPDATED";

    public ThirdUpdatedEvent(ThirdUpdatedEventDto data) {
        super(data, TYPE);
    }
}
