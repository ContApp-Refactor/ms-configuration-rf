package co.unicauca.edu.co.contables.auxiliary.domain.models.log;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para un registro de log del libro auxiliar.
 *
 * Captura un evento puntual ({@link ETypeEvent}) asociado a un libro,
 * con un mensaje descriptivo, para la auditoría de las operaciones
 * de registro, generación, exportación y envío.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuxiliaryBookLog {
    private Long id;
    private String publicId;
    private AuxiliaryBook auxiliaryBook;
    private ETypeEvent ETypeEvent;
    private String message;
}
