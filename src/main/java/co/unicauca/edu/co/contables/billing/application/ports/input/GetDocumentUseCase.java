package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.UUID;

/** Consulta de un documento (QRY-1). */
public interface GetDocumentUseCase {

    BillingDocumentView get(String enterpriseId, DocumentType type, UUID id);
}
