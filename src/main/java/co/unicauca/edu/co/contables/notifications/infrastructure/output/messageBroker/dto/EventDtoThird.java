package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Generic Event Data Transfer Object (DTO) for message broker communication with two type parameters.
 * @param <T> The type of the data payload.
 * @param <U> The type of the event type.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDtoThird<T, U> {
    private T data;
    private U type;
}