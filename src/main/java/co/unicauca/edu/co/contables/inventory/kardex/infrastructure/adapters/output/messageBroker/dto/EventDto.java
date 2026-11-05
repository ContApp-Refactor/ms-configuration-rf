package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto<T, U> {
    private T data;
    private U type;
}
