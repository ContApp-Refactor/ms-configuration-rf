package co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO genérico para eventos
 *
 * DTO genérico que encapsula datos de eventos y su tipo
 * para el sistema de mensajería de centros de costo.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto<T, U> {    
    
    private T data;
    private U type;
}
