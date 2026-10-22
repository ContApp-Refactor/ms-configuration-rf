package co.unicauca.edu.co.contables.configuration.thirds.application.service.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.UpdateTypeIdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdInUseException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateTypeIdService implements UpdateTypeIdUseCase {

    private final IdOutputPort idOutputPort;

    /**
     * @brief Actualiza un tipo de identificación existente en el sistema
     * @param typeId tipo de identificación con los datos actualizados
     * @return tipo de identificación actualizado
     * @throws TypeIdNotFound si el tipo de identificación no existe
     * @throws TypeIdInUseException si el tipo de identificación tiene terceros con movimientos contables
     */
    @Auditable(operationType = OperationType.UPDATE, affectedTable = "TYPE_ID")
    @Override
    public TypeId updateTypeId(TypeId typeId) {
        if (typeId == null || typeId.getId() == null) {
            throw new IllegalArgumentException("El tipo de identificación y su ID son requeridos para actualizar");
        }

        // Verificar que el tipo de identificación existe
        TypeId existingTypeId = validateTypeIdExists(typeId.getId());

        // Verificar que el tipo de identificación no tenga terceros con movimientos contables asociados
        validateTypeIdNotInUseForUpdate(typeId.getId(), existingTypeId.getEntId(), existingTypeId.getTypeId());

        // Actualizar el tipo de identificación
        TypeId updatedTypeId = idOutputPort.updateTypeId(typeId);

        return updatedTypeId;
    }

    /**
     * @brief Valida que el tipo de identificación existe en el sistema
     * @param typeIdId identificador único del tipo de identificación
     * @return tipo de identificación encontrado
     * @throws TypeIdNotFound si el tipo de identificación no existe
     */
    private TypeId validateTypeIdExists(Long typeIdId) {
        TypeId typeId = idOutputPort.getTypeIdById(typeIdId);
        if (typeId == null) {
            throw new TypeIdNotFound("El tipo de identificación con ID " + typeIdId + " no existe");
        }
        return typeId;
    }

    /**
     * @brief Valida que el tipo de identificación no tenga terceros con movimientos contables asociados
     * @param typeIdId identificador único del tipo de identificación
     * @param entId identificador de la empresa
     * @param typeIdName nombre del tipo de identificación para mensajes de error
     * @throws TypeIdInUseException si el tipo de identificación tiene terceros con movimientos contables
     */
    private void validateTypeIdNotInUseForUpdate(Long typeIdId, String entId, String typeIdName) {
        boolean hasThirdsWithMovements = idOutputPort.hasTypeIdThirdsWithMovements(typeIdId, entId);

        if (hasThirdsWithMovements) {
            throw new TypeIdInUseException(typeIdName, true); // true indica operación de edición
        }
    }
}
