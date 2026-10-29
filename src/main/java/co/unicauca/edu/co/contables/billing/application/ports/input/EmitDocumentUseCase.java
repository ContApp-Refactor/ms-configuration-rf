package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.UUID;

/** Emision de un documento en BORRADOR (EMT-1..4): asigna numero consecutivo y congela el contenido. */
public interface EmitDocumentUseCase {

    /**
     * @param version version esperada del borrador ({@code null} omite la verificacion)
     * @return el documento emitido tal como quedo persistido
     */
    BillingDocument emit(String enterpriseId, DocumentType type, UUID id, Long version);
}
