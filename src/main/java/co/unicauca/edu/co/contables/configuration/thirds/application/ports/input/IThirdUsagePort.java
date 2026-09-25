package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

/**
 * @brief Puerto de entrada para operaciones de uso de terceros
 */
public interface IThirdUsagePort {

    /**
     * @brief Incrementa el contador de uso del tercero
     * @param thirdId ID del tercero
     */
    void incrementUsageCount(Long thirdId);
}
