package co.unicauca.edu.co.contables.billing.application.ports.input.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Datos de un borrador de devolucion. {@code originalDocumentId} es obligatorio al crear; al
 * actualizar puede omitirse (se usa la original ya guardada) pero no puede cambiarse.
 * {@code issueDate} nula = hoy al crear, conserva la actual al actualizar. {@code lines} nula o
 * vacia es un borrador parcial (RET-4). {@code expectedVersion} nula omite el control optimista.
 */
public record ReturnDraftCommand(
        UUID originalDocumentId,
        LocalDate issueDate,
        String reason,
        List<ReturnLineCommand> lines,
        Long expectedVersion) {
}
