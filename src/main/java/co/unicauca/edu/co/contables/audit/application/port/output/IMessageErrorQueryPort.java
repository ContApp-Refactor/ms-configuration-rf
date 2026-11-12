package co.unicauca.edu.co.contables.audit.application.port.output;

import java.util.Optional;

import co.unicauca.edu.co.contables.audit.application.dto.response.MessageErrorAuditResponse;

/**
 * @brief Puerto de salida para la consulta y limpieza de los errores de
 *        procesamiento de mensajes de auditoria.
 */
public interface IMessageErrorQueryPort {

    /**
     * @brief Devuelve el error de procesamiento mas reciente ordenado por
     *        fecha de error descendente.
     * @return El error mas reciente, o vacio si no hay registros.
     */
    Optional<MessageErrorAuditResponse> findLatest();

    /**
     * @brief Elimina todos los registros de errores de procesamiento.
     */
    void deleteAll();
}
