package co.unicauca.edu.co.contables.configuration.thirds.application.service.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.DeleteTypeIdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdInUseException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @brief Servicio para eliminar tipos de identificación
 *
 * Implementa las validaciones de negocio necesarias antes de la eliminación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteTypeIdService implements DeleteTypeIdUseCase {

    private final IdOutputPort idOutputPort;

    /**
     * @brief Elimina un tipo de identificación del sistema con validaciones completas
     * @param typeIdId identificador único del tipo de identificación a eliminar
     * @param entId identificador de la empresa
     * @return true si se eliminó correctamente
     * @throws TypeIdNotFound si el tipo de identificación no existe
     * @throws TypeIdInUseException si el tipo de identificación está siendo utilizado
     */
    @Auditable(operationType = OperationType.DELETE, affectedTable = "TYPE_ID", idArgIndex = 0, enterpriseIdArgIndex = 1)
    @Override
    @Transactional
    public boolean deleteTypeId(Long typeIdId, String entId) {
        // Verificar que el tipo de identificación existe (validación de negocio)
        TypeId typeId = validateTypeIdExists(typeIdId, entId);

        // Verificar que el tipo de identificación no esté siendo utilizado (validación de negocio)
        validateTypeIdNotInUse(typeIdId, entId, typeId.getTypeId());

        // Proceder con la eliminación
        return idOutputPort.deleteTypeId(typeIdId, entId);
    }

    /**
     * @brief Valida que el tipo de identificación existe en el sistema
     * @param typeIdId identificador único del tipo de identificación
     * @param entId identificador de la empresa
     * @return tipo de identificación encontrado
     * @throws TypeIdNotFound si el tipo de identificación no existe
     */
    private TypeId validateTypeIdExists(Long typeIdId, String entId) {
        if (!idOutputPort.existsTypeIdById(typeIdId)) {
            throw new TypeIdNotFound("El tipo de identificación con ID " + typeIdId + " no existe");
        }

        TypeId typeId = idOutputPort.getTypeIdById(typeIdId);
        if (typeId == null || !entId.equals(typeId.getEntId())) {
            throw new TypeIdNotFound(
                    "El tipo de identificación con ID " + typeIdId + " no existe para la empresa " + entId);
        }

        return typeId;
    }

    /**
     * @brief Valida que el tipo de identificación no esté siendo utilizado por terceros existentes
     * @param typeIdId identificador único del tipo de identificación
     * @param entId identificador de la empresa
     * @param typeIdName nombre del tipo de identificación para mensajes de error
     * @throws TypeIdInUseException si el tipo de identificación está siendo utilizado
     */
    private void validateTypeIdNotInUse(Long typeIdId, String entId, String typeIdName) {
        boolean isInUse = idOutputPort.isTypeIdInUse(typeIdId, entId);

        if (isInUse) {
            throw new TypeIdInUseException(typeIdName);
        }

    }
}
