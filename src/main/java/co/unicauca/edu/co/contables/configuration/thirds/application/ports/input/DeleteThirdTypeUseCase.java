package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeInUseException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeNotFound;

/**
 * @brief Caso de uso para eliminación de tipos de tercero
 *
 * Permite eliminar tipos de tercero del sistema con validación
 * de integridad referencial para evitar eliminación de tipos en uso.
 */
public interface DeleteThirdTypeUseCase {
    /**
     * @brief Elimina un tipo de tercero del sistema
     *
     * Valida que el tipo de tercero no esté siendo utilizado por terceros existentes
     * antes de proceder con la eliminación.
     * 
     * @param thirdTypeId el ID del tipo de tercero a eliminar
     * @param entId el ID de la empresa
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws ThirdTypeNotFound si el tipo de tercero no existe
     * @throws ThirdTypeInUseException si el tipo de tercero está siendo utilizado
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    boolean deleteThirdType(Long thirdTypeId, String entId);
}
