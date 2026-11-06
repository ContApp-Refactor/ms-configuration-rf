package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.List;
import java.util.UUID;

/** Listado de resumenes por tipo, con filtros opcionales de estado y documento original (QRY-1, DRF-7). */
public interface ListDocumentsUseCase {

    List<BillingDocumentSummary> list(String enterpriseId, DocumentType type, DocumentStatus status,
            UUID originalDocumentId);
}
