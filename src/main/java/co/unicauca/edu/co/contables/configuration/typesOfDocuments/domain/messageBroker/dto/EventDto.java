package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO genérico para eventos de message broker
 *
 * Estructura genérica que encapsula eventos con tipo de operación
 * y datos asociados para comunicación vía RabbitMQ.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto<T, U> {
    private T data;
    private U type;
}
