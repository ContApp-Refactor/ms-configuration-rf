package co.unicauca.edu.co.contables.billing.application.ports.input.command;

import java.time.LocalDate;
import java.util.List;

/**
 * Contenido completo de un borrador de factura (reemplazo total, DRF-2). {@code counterpartyId}
 * nulo = sin tercero; {@code issueDate} nula = hoy al crear, o conservar la actual al actualizar;
 * {@code lines} nula = sin lineas; {@code expectedVersion} nula omite el control optimista.
 */
public record InvoiceDraftCommand(
        Long counterpartyId,
        LocalDate issueDate,
        String supplierInvoiceNumber,
        List<InvoiceLineCommand> lines,
        Long expectedVersion) {
}
