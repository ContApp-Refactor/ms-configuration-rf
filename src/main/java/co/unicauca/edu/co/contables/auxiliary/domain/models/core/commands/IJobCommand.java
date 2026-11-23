package co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands;

/**
 * @brief Contrato del patrón Command para los pasos de un job programado.
 *
 * Cada implementación representa una etapa (generación, descarga, envío
 * por correo) del pipeline de reportes programados, operando sobre un
 * {@link JobCommandContext} compartido que transporta atributos entre
 * pasos y persiste el resultado de la ejecución.
 */
public interface IJobCommand {
    void execute(JobCommandContext context);
}
