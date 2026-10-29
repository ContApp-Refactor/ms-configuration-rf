package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.UUID;

/** Anulacion de un documento EMITIDA con motivo obligatorio (ANL-1, ANL-2); conserva el numero. */
public interface AnnulDocumentUseCase {

    /**
     * @param reason  motivo (se recorta; 1..500 caracteres)
     * @param version version esperada del documento ({@code null} omite la verificacion)
     * @return el documento anulado tal como quedo persistido
     */
    BillingDocument annul(String enterpriseId, DocumentType type, UUID id, String reason, Long version);
}
