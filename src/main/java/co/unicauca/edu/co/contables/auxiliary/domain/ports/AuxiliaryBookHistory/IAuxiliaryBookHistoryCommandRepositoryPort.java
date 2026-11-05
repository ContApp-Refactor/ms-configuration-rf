package co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;

/**
 * @brief Puerto de salida para operaciones de escritura del historial de libros auxiliares.
 *
 * Define el contrato para persistir y actualizar registros históricos
 * del ciclo de vida de un libro auxiliar en el sistema de almacenamiento.
 */
public interface IAuxiliaryBookHistoryCommandRepositoryPort {
    /**
     * @brief Persiste un registro de historial del libro auxiliar.
     * @param auxiliaryBookHistory Registro de historial a guardar.
     * @return El historial guardado con los identificadores generados.
     */
    AuxiliaryBookHistory registerAuxiliaryBookHistory(AuxiliaryBookHistory auxiliaryBookHistory);

    /**
     * @brief Actualiza un registro de historial existente.
     * @param auxiliaryBookHistory Registro de historial a actualizar.
     * @return El historial actualizado.
     */
    AuxiliaryBookHistory updateAuxiliaryBookHistory(AuxiliaryBookHistory auxiliaryBookHistory);
}
