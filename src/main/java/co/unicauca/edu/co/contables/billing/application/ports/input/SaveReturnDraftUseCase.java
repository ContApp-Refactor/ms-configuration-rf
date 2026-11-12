package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnDraftCommand;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.UUID;

/** Crea y actualiza (autosave) borradores de devolucion (NC / DC) contra una factura EMITIDA. */
public interface SaveReturnDraftUseCase {

    /** {@code type} debe ser SALE_RETURN o PURCHASE_RETURN; la original viene en el comando. */
    BillingDocument create(String enterpriseId, DocumentType type, ReturnDraftCommand command);

    BillingDocument update(String enterpriseId, DocumentType type, UUID id, ReturnDraftCommand command);
}
