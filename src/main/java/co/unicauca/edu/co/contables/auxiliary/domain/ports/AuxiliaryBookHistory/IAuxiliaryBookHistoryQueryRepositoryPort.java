package co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @brief Puerto de salida para operaciones de consulta del historial de libros auxiliares.
 *
 * Define el contrato para recuperar registros históricos de libros
 * auxiliares desde el sistema de almacenamiento, con soporte de paginación
 * y búsqueda por identificador interno del libro.
 */
public interface IAuxiliaryBookHistoryQueryRepositoryPort {
    /**
     * @brief Recupera una página de registros de historial para una entidad específica.
     * @param entId Identificador de la entidad cuyos historiales se consultan.
     * @param pageable Información de paginación.
     * @return Página de registros {@link AuxiliaryBookHistory} para la entidad dada.
     */
    Page<AuxiliaryBookHistory> findPageByEntId(String entId, Pageable pageable);

    /**
     * @brief Busca el historial de un libro por el ID interno del libro.
     * @param bookId El ID (Long) interno del AuxiliaryBook.
     * @return El AuxiliaryBookHistory, o null si no se encuentra.
     */
    AuxiliaryBookHistory findByBookId(Long bookId);
}
