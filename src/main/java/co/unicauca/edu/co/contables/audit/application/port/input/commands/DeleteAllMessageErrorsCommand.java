package co.unicauca.edu.co.contables.audit.application.port.input.commands;

/**
 * @brief Elimina todos los registros de errores de procesamiento de mensajes de
 *        auditoria.
 */
public interface DeleteAllMessageErrorsCommand {

    void execute();
}
