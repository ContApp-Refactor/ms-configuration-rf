package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

/**
 * @brief Caso de uso para cambio individual del estado de un tercero
 *
 * Permite activar o inactivar un tercero específico
 * perteneciente a una empresa.
 */
public interface ChangeThirdStateUseCase {
    /**
     * @brief Cambia el estado de un tercero específico
     * @param thId Identificador del tercero
     * @param entId Identificador de la empresa
     * @return true si el estado cambió, false en caso contrario
     */
    boolean changeThirdState(Long thId, String entId);
}
