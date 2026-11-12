package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.UUID;

/** Duplica un borrador de factura en un borrador nuevo (DRF-6). */
public interface DuplicateInvoiceDraftUseCase {

    BillingDocument duplicate(String enterpriseId, DocumentType type, UUID id);
}
