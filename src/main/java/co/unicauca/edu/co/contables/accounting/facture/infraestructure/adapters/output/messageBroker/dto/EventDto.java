package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto<T, U> {
    private U type;
    private T data;
}
