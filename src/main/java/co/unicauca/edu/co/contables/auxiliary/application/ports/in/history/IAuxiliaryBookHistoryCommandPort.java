package co.unicauca.edu.co.contables.auxiliary.application.ports.in.history;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;

/**
 * @brief Puerto de entrada para la escritura del historial de libros auxiliares.
 *
 * Define el contrato, dentro de la capa de aplicación, para registrar
 * y actualizar los registros del historial (estado y trazabilidad) de
 * los libros auxiliares generados.
 */
public interface IAuxiliaryBookHistoryCommandPort {
    /**
     * @brief Registra un nuevo registro de historial de libro auxiliar.
     * @param auxiliaryBookHistory Historial a registrar.
     * @return Historial registrado con los identificadores generados.
     */
    AuxiliaryBookHistory registerAuxiliaryBookHistory(AuxiliaryBookHistory auxiliaryBookHistory);

    /**
     * @brief Actualiza un registro existente de historial de libro auxiliar.
     * @param auxiliaryBookHistory Historial con los datos actualizados.
     * @return Historial persistido con los cambios aplicados.
     */
    AuxiliaryBookHistory updateAuxiliaryBookHistory(AuxiliaryBookHistory auxiliaryBookHistory);
}
