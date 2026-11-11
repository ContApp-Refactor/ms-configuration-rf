package co.unicauca.edu.co.contables.billing.application.ports.input.command;

import co.unicauca.edu.co.contables.billing.domain.model.WithholdingCode;
import java.time.LocalDate;
import java.util.List;

/**
 * Contenido completo de un borrador de factura (reemplazo total, DRF-2). {@code counterpartyId}
 * nulo = sin tercero; {@code issueDate} nula = hoy al crear, o conservar la actual al actualizar;
 * {@code lines} nula = sin lineas; {@code expectedVersion} nula omite el control optimista.
 * {@code payment} (forma de pago) solo se aplica a la factura de VENTA; en los demas tipos se
 * ignora. Nulo = sin forma de pago todavia. {@code withholdings} (retenciones) solo aplica a facturas;
 * reemplazo total: nulo o vacio = sin retenciones.
 */
public record InvoiceDraftCommand(
        Long counterpartyId,
        LocalDate issueDate,
        String supplierInvoiceNumber,
        List<InvoiceLineCommand> lines,
        Long expectedVersion,
        PaymentCommand payment,
        List<WithholdingCommand> withholdings) {

    /** Sin retenciones. */
    public InvoiceDraftCommand(Long counterpartyId, LocalDate issueDate, String supplierInvoiceNumber,
            List<InvoiceLineCommand> lines, Long expectedVersion, PaymentCommand payment) {
        this(counterpartyId, issueDate, supplierInvoiceNumber, lines, expectedVersion, payment, null);
    }

    /** Sin forma de pago (compras y clientes que aun no la envian). */
    public InvoiceDraftCommand(Long counterpartyId, LocalDate issueDate, String supplierInvoiceNumber,
            List<InvoiceLineCommand> lines, Long expectedVersion) {
        this(counterpartyId, issueDate, supplierInvoiceNumber, lines, expectedVersion, null);
    }

    /** Retencion pedida: clase y el impuesto de la empresa cuya tarifa se aplica al subtotal. */
    public record WithholdingCommand(WithholdingCode code, Long taxId) {
    }

    /** Metodo de pago pedido en el borrador . */
    public record PaymentCommand(Long paymentMethodId) {
    }
}
