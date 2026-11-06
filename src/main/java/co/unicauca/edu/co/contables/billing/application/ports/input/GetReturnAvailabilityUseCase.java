package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import java.util.UUID;

/** Cuanto de cada linea de una factura puede devolverse todavia (RET-9). */
public interface GetReturnAvailabilityUseCase {

    /** {@code invoiceType} debe ser SALE_INVOICE o PURCHASE_INVOICE. */
    ReturnAvailability availability(String enterpriseId, DocumentType invoiceType, UUID invoiceId);
}
