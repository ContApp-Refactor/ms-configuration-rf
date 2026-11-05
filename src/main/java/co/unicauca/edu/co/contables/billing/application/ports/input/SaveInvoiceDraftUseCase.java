package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.UUID;

/** Crea y actualiza (autosave) borradores de factura de venta o compra (DRF-1, DRF-2). */
public interface SaveInvoiceDraftUseCase {

    BillingDocument create(String enterpriseId, DocumentType type, InvoiceDraftCommand command);

    BillingDocument update(String enterpriseId, DocumentType type, UUID id, InvoiceDraftCommand command);
}
