package co.unicauca.edu.co.contables.audit.application.usecases.queries;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.audit.application.dto.response.MessageErrorAuditResponse;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.GetLatestMessageErrorQuery;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorQueryPort;

/**
 * @brief Implementa la consulta del error de procesamiento de mensajes mas
 *        reciente de auditoria.
 */
@Service
public class GetLatestMessageErrorQueryImpl implements GetLatestMessageErrorQuery {

    private final IMessageErrorQueryPort queryPort;

    public GetLatestMessageErrorQueryImpl(IMessageErrorQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessageErrorAuditResponse> execute() {
        return queryPort.findLatest();
    }
}
