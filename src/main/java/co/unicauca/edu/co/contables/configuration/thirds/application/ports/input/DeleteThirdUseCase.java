package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdNotFound;

/**
 * @brief Caso de uso para eliminación de terceros
 *
 * Permite eliminar terceros del sistema con validación
 * de integridad referencial para evitar eliminación de terceros con dependencias.
 */
public interface DeleteThirdUseCase {
    /**
     * @brief Elimina un tercero del sistema
     *
     * Valida que el tercero no tenga dependencias o relaciones activas
     * antes de proceder con la eliminación.
     * 
     * @param thirdId el ID del tercero a eliminar
     * @param entId el ID de la empresa
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws ThirdNotFound si el tercero no existe
     * @throws com.thirdsmanagement.thirds.domain.exceptions.third.ThirdHasDependenciesException si el tercero tiene dependencias
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    boolean deleteThird(Long thirdId, String entId);
}
