package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @brief DTO genérico para eventos de message broker
 *
 * Estructura genérica que encapsula eventos con tipo de operación
 * y datos asociados para comunicación vía RabbitMQ.
 * Compatible con ms-debt-payments EventDto.
 */
@Data
@AllArgsConstructor
public class EventDto<T> {
    private String type;
    private T data;
}
