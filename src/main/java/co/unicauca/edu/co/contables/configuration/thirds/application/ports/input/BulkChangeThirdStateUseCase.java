package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

/**
 * @brief Caso de uso para cambio masivo del estado de terceros
 *
 * Permite activar o inactivar todos los terceros pertenecientes
 * a una empresa de forma masiva y eficiente.
 */
public interface BulkChangeThirdStateUseCase {

    /**
     * @brief Cambia el estado de todos los terceros de una empresa
     * @param entId ID de la empresa
     * @param newState Nuevo estado (true para activo, false para inactivo)
     * @return Cantidad de terceros actualizados
     */
    int changeAllThirdsState(String entId, Boolean newState);
}
