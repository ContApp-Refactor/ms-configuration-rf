package co.unicauca.edu.co.contables.configuration.thirds.application.service.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.DeleteThirdTypeUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeInUseException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @brief Servicio para eliminar tipos de tercero
 *
 * Implementa las validaciones de negocio necesarias antes de la eliminación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteThirdTypeService implements DeleteThirdTypeUseCase {

    private final IdOutputPort idOutputPort;

    @Auditable(operationType = OperationType.DELETE, affectedTable = "THIRD_TYPE")
    @Override
    @Transactional
    public boolean deleteThirdType(Long thirdTypeId, String entId) {
        // Verificar que el tipo de tercero existe (validación de negocio)
        ThirdType thirdType = validateThirdTypeExists(thirdTypeId, entId);

        // Verificar que el tipo de tercero no esté siendo utilizado (validación de negocio)
        validateThirdTypeNotInUse(thirdTypeId, entId, thirdType.getThirdTypeName());

        // Proceder con la eliminación
        return idOutputPort.deleteThirdType(thirdTypeId, entId);
    }

    /**
     * @brief Valida que el tipo de tercero existe en el sistema
     * @param thirdTypeId identificador único del tipo de tercero
     * @param entId identificador de la empresa
     * @return tipo de tercero encontrado
     * @throws ThirdTypeNotFound si el tipo de tercero no existe
     */
    private ThirdType validateThirdTypeExists(Long thirdTypeId, String entId) {
        if (!idOutputPort.existsThirdTypeById(thirdTypeId)) {
            throw new ThirdTypeNotFound("El tipo de tercero con ID " + thirdTypeId + " no existe");
        }

        ThirdType thirdType = idOutputPort.getThirdTypeById(thirdTypeId);
        if (thirdType == null || !entId.equals(thirdType.getEntId())) {
            throw new ThirdTypeNotFound(
                    "El tipo de tercero con ID " + thirdTypeId + " no existe para la empresa " + entId);
        }

        return thirdType;
    }

    /**
     * @brief Valida que el tipo de tercero no esté siendo utilizado por terceros existentes
     * @param thirdTypeId identificador único del tipo de tercero
     * @param entId identificador de la empresa
     * @param thirdTypeName nombre del tipo de tercero para mensajes de error
     * @throws ThirdTypeInUseException si el tipo de tercero está siendo utilizado
     */
    private void validateThirdTypeNotInUse(Long thirdTypeId, String entId, String thirdTypeName) {
        boolean isInUse = idOutputPort.isThirdTypeInUse(thirdTypeId, entId);

        if (isInUse) {
            throw new ThirdTypeInUseException(thirdTypeName);
        }
    }
}
