package co.unicauca.edu.co.contables.auxiliary.application.ports.in.auxiliaryBook;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;

import java.util.List;

/**
 * @brief Puerto de entrada para operaciones de escritura del Libro Auxiliar.
 *
 * Define el contrato, dentro de la capa de aplicación, para registrar
 * libros auxiliares y generar la información contable asociada a ellos.
 */
public interface IAuxiliaryBookCommandPort {
    /**
     * @brief Registra un nuevo libro auxiliar.
     * @param auxiliaryBook Libro auxiliar a registrar.
     * @return Libro auxiliar registrado con los identificadores generados.
     */
    AuxiliaryBook registerAuxiliaryBook(AuxiliaryBook auxiliaryBook);

    /**
     * @brief Genera la información contable de un libro auxiliar.
     * @param auxiliaryBook Libro auxiliar para el que se genera la información.
     * @return Lista con la información generada según el tipo de libro.
     */
    List<?> genereteAuxiliaryBookInfo(AuxiliaryBook auxiliaryBook);
}
