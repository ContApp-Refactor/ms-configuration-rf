package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.ReturnSummary;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.List;
import java.util.UUID;

/** Devoluciones de una factura, numeradas primero (por ordinal) y borradores al final (RET-10, RET-11). */
public interface ListDocumentReturnsUseCase {

    /** {@code invoiceType} debe ser SALE_INVOICE o PURCHASE_INVOICE. */
    List<ReturnSummary> returnsOf(String enterpriseId, DocumentType invoiceType, UUID invoiceId);
}
