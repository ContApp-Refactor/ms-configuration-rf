package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdInUseException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdNotFound;

/**
 * @brief Caso de uso para eliminación de tipos de identificación
 *
 * Permite eliminar tipos de identificación del sistema con validación
 * de integridad referencial para evitar eliminación de tipos en uso.
 */
public interface DeleteTypeIdUseCase {
    /**
     * @brief Elimina un tipo de identificación del sistema
     *
     * Valida que el tipo de identificación no esté siendo utilizado por terceros existentes
     * antes de proceder con la eliminación.
     * 
     * @param typeIdId el ID del tipo de identificación a eliminar
     * @param entId el ID de la empresa
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws TypeIdNotFound si el tipo de identificación no existe
     * @throws TypeIdInUseException si el tipo de identificación está siendo utilizado
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    boolean deleteTypeId(Long typeIdId, String entId);
}
