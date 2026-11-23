package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Generic wrapper for message broker events
 * 
 * Encapsulates the event payload and its type for transmission
 * over the message bus.
 * 
 * @param <T> The type of the data payload
 * @param <U> The type of the event identifier (usually an Enum)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto<T,U> {
    /**
     * @brief The event payload data
     */
    private T data;

    /**
     * @brief The type or category of the event
     */
    private U type;
    
}
