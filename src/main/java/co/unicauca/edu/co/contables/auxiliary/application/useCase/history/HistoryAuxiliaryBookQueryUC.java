package co.unicauca.edu.co.contables.auxiliary.application.useCase.history;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.history.IAuxiliaryBookHistoryQueryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @brief Caso de uso de consulta para el historial de Libros Auxiliares.
 *
 * Implementa el puerto de entrada {@link IAuxiliaryBookHistoryQueryPort}
 * y consulta de manera paginada los registros históricos por entidad,
 * validando los parámetros de entrada antes de delegar en el repositorio.
 */
@Service
@RequiredArgsConstructor
public class HistoryAuxiliaryBookQueryUC implements IAuxiliaryBookHistoryQueryPort {

    private final IAuxiliaryBookHistoryQueryRepositoryPort abHistoryQueryRepositoryPort;

    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageServicePort;

    @Override
    public Page<AuxiliaryBookHistory> findPageByEntId(String entId, Pageable pageable) {
        if(entId.isEmpty()){
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(404, this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED));
        }
        return this.abHistoryQueryRepositoryPort.findPageByEntId(entId,pageable);
    }
}
