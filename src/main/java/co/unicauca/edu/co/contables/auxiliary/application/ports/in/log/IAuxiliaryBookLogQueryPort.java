package co.unicauca.edu.co.contables.auxiliary.application.ports.in.log;

import java.util.List;

import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;

/**
 * @brief Puerto de entrada para la consulta de logs del libro auxiliar.
 *
 * Define el contrato, dentro de la capa de aplicación, para recuperar
 * las trazas y eventos de log registrados durante la generación de un
 * libro auxiliar específico.
 */
public interface IAuxiliaryBookLogQueryPort {
    /**
     * @brief Recupera todos los logs asociados a un libro auxiliar.
     * @param auxiliaryBookId Identificador público del libro auxiliar.
     * @return Lista de registros de log pertenecientes al libro indicado.
     */
    List<AuxiliaryBookLog> findAllByAuxiliaryBookPublicId(String auxiliaryBookId);
}
