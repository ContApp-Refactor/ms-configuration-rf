package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.InvoiceWithReturnsSummary;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.List;

/** Facturas del tipo dado que tienen al menos una devolucion EMITIDA (RET-11). */
public interface ListInvoicesWithReturnsUseCase {

    /** {@code invoiceType} debe ser SALE_INVOICE o PURCHASE_INVOICE. */
    List<InvoiceWithReturnsSummary> list(String enterpriseId, DocumentType invoiceType);
}
