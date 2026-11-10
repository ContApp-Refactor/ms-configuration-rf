package co.unicauca.edu.co.contables.billing.application.ports.input.command;

import java.time.LocalDate;
import java.util.List;

/**
 * Contenido completo de un borrador de factura (reemplazo total, DRF-2). {@code counterpartyId}
 * nulo = sin tercero; {@code issueDate} nula = hoy al crear, o conservar la actual al actualizar;
 * {@code lines} nula = sin lineas; {@code expectedVersion} nula omite el control optimista.
 * {@code payment} (forma de pago) solo se aplica a la factura de VENTA; en los demas tipos se
 * ignora. Nulo = sin forma de pago todavia.
 */
public record InvoiceDraftCommand(
        Long counterpartyId,
        LocalDate issueDate,
        String supplierInvoiceNumber,
        List<InvoiceLineCommand> lines,
        Long expectedVersion,
        PaymentCommand payment) {

    /** Sin forma de pago (compras y clientes que aun no la envian). */
    public InvoiceDraftCommand(Long counterpartyId, LocalDate issueDate, String supplierInvoiceNumber,
            List<InvoiceLineCommand> lines, Long expectedVersion) {
        this(counterpartyId, issueDate, supplierInvoiceNumber, lines, expectedVersion, null);
    }

    /** Metodo de pago pedido en el borrador . */
    public record PaymentCommand(Long paymentMethodId) {
    }
}
