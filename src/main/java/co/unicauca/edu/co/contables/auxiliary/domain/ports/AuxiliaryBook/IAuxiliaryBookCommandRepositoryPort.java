package co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBook;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;

/**
 * @brief Puerto de salida para operaciones de escritura del libro auxiliar.
 *
 * Define el contrato para persistir libros auxiliares en el sistema
 * de almacenamiento.
 */
public interface IAuxiliaryBookCommandRepositoryPort {
    /**
     * @brief Persiste un nuevo registro de libro auxiliar.
     * @param auxiliaryBook Libro auxiliar a guardar.
     * @return Libro auxiliar guardado con los identificadores generados.
     */
    AuxiliaryBook registerAuxiliaryBook(AuxiliaryBook auxiliaryBook);
}
