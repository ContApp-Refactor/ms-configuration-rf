package co.unicauca.edu.co.contables.configuration.thirds.application.service.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.UpdateThirdTypeUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeInUseException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @brief Servicio para la actualización de tipos de tercero
 *
 * Implementa el caso de uso UpdateThirdTypeUseCase con validaciones de negocio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateThirdTypeService implements UpdateThirdTypeUseCase {

    private final IdOutputPort idOutputPort;

    /**
     * @brief Actualiza un tipo de tercero existente en el sistema
     * @param thirdType tipo de tercero con los datos actualizados
     * @return tipo de tercero actualizado
     * @throws ThirdTypeNotFound si el tipo de tercero no existe
     * @throws ThirdTypeInUseException si el tipo de tercero tiene terceros con movimientos contables
     */
    @Auditable(operationType = OperationType.UPDATE, affectedTable = "THIRD_TYPE")
    @Override
    public ThirdType updateThirdType(ThirdType thirdType) {
        if (thirdType == null || thirdType.getThirdTypeId() == null) {
            throw new IllegalArgumentException("El tipo de tercero y su ID son requeridos para actualizar");
        }

        // Verificar que el tipo de tercero existe
        ThirdType existingThirdType = validateThirdTypeExists(thirdType.getThirdTypeId());

        // Verificar que el tipo de tercero no tenga terceros con movimientos contables asociados
        validateThirdTypeNotInUseForUpdate(thirdType.getThirdTypeId(), existingThirdType.getEntId(), existingThirdType.getThirdTypeName());

        // Actualizar el tipo de tercero
        ThirdType updatedThirdType = idOutputPort.updateThirdType(thirdType);

        return updatedThirdType;
    }

    /**
     * @brief Valida que el tipo de tercero existe en el sistema
     * @param thirdTypeId identificador único del tipo de tercero
     * @return tipo de tercero encontrado
     * @throws ThirdTypeNotFound si el tipo de tercero no existe
     */
    private ThirdType validateThirdTypeExists(Long thirdTypeId) {
        ThirdType thirdType = idOutputPort.getThirdTypeById(thirdTypeId);
        if (thirdType == null) {
            throw new ThirdTypeNotFound("El tipo de tercero con ID " + thirdTypeId + " no existe");
        }
        return thirdType;
    }

    /**
     * @brief Valida que el tipo de tercero no tenga terceros con movimientos contables asociados
     * @param thirdTypeId identificador único del tipo de tercero
     * @param entId identificador de la empresa
     * @param thirdTypeName nombre del tipo de tercero para mensajes de error
     * @throws ThirdTypeInUseException si el tipo de tercero tiene terceros con movimientos contables
     */
    private void validateThirdTypeNotInUseForUpdate(Long thirdTypeId, String entId, String thirdTypeName) {
        boolean hasThirdsWithMovements = idOutputPort.hasThirdTypeThirdsWithMovements(thirdTypeId, entId);

        if (hasThirdsWithMovements) {
            throw new ThirdTypeInUseException(thirdTypeName, true); // true indica operación de edición
        }
    }
}
