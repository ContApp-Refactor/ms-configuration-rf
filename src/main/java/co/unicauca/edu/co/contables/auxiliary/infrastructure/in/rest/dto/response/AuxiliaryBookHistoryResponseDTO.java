package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @brief DTO de respuesta con el historial de un libro auxiliar.
 *
 * Expone los campos relevantes de un registro histórico: identificadores,
 * el libro auxiliar asociado, el estado actual del historial y la vía
 * de entrega utilizada.
 */
@Data
@AllArgsConstructor
public class AuxiliaryBookHistoryResponseDTO {
    private Long id;
    private String publicId;
    private AuxiliaryBook auxiliaryBook;
    private EState state;
    private EDeliveryWay deliveryWay;
}
