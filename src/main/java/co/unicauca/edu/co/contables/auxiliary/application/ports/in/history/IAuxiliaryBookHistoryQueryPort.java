package co.unicauca.edu.co.contables.auxiliary.application.ports.in.history;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @brief Puerto de entrada para la consulta del historial de libros auxiliares.
 *
 * Define el contrato, dentro de la capa de aplicación, para recuperar
 * de forma paginada el historial de libros auxiliares asociado a una
 * entidad (organización o compañía).
 */
public interface IAuxiliaryBookHistoryQueryPort {
    /**
     * @brief Recupera paginadamente el historial de una entidad.
     * @param entId Identificador de la entidad dueña del historial.
     * @param pageable Información de paginación y ordenamiento.
     * @return Página de registros de historial para la entidad indicada.
     */
    Page<AuxiliaryBookHistory> findPageByEntId(String entId, Pageable pageable);
}
