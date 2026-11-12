package co.unicauca.edu.co.contables.audit.application.usecases.commands;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.audit.application.port.input.commands.DeleteAllMessageErrorsCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorQueryPort;

/**
 * @brief Implementa la limpieza total de los errores de procesamiento de
 *        mensajes de auditoria.
 */
@Service
public class DeleteAllMessageErrorsCommandImpl implements DeleteAllMessageErrorsCommand {

    private final IMessageErrorQueryPort queryPort;

    public DeleteAllMessageErrorsCommandImpl(IMessageErrorQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional
    public void execute() {
        queryPort.deleteAll();
    }
}
