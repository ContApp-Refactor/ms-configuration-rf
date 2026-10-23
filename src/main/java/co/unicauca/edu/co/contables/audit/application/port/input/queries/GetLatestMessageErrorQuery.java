package co.unicauca.edu.co.contables.audit.application.port.input.queries;

import java.util.Optional;

import co.unicauca.edu.co.contables.audit.application.dto.response.MessageErrorAuditResponse;

/**
 * @brief Consulta el error de procesamiento de mensajes mas reciente de
 *        auditoria.
 * @return El error mas reciente, o vacio si no hay registros.
 */
public interface GetLatestMessageErrorQuery {

    Optional<MessageErrorAuditResponse> execute();
}
