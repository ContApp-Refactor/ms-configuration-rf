package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.UUID;

/** Elimina fisicamente un documento en BORRADOR (DRF-5). {@code version} nula omite el control. */
public interface DeleteDocumentDraftUseCase {

    void delete(String enterpriseId, DocumentType type, UUID id, Long version);
}
