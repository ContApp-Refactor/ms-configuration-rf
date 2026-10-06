package co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog;

import java.util.List;

import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;

/**
 * @brief Puerto de salida para operaciones de consulta de logs del libro auxiliar.
 *
 * Define el contrato para recuperar los eventos asociados a un libro
 * auxiliar desde el sistema de almacenamiento.
 */
public interface IAuxiliaryBookLogQueryRepositoryPort {
    /**
     * @brief Recupera los logs asociados a un libro auxiliar por su publicId.
     * @param auxiliaryBookId Identificador público del libro auxiliar.
     * @return Lista de registros {@link AuxiliaryBookLog} asociados al libro.
     */
    List<AuxiliaryBookLog> findAllByAuxiliaryBookPublicId(String auxiliaryBookId);
}
