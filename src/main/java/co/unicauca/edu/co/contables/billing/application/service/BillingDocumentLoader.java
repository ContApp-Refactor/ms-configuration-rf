package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.Objects;
import java.util.UUID;

/**
 * Carga un documento y aplica las guardas comunes: existencia, tipo (un documento de otro tipo se
 * comporta como inexistente, PUR-3) y version esperada (DRF-3).
 */
public class BillingDocumentLoader {

    private final BillingDocumentRepositoryPort repository;

    public BillingDocumentLoader(BillingDocumentRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /** Carga sin control de version. */
    public BillingDocument load(String enterpriseId, DocumentType type, UUID id) {
        BillingDocument document = repository.findById(enterpriseId, id)
                .orElseThrow(() -> new BillingException(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND,
                        "Documento no encontrado"));
        document.ensureType(type);
        return document;
    }

    /**
     * Carga BLOQUEANDO la fila del documento (D12) y luego aplica tipo y version esperada. La
     * version se verifica despues de tomar el bloqueo, asi que compara contra el estado vigente.
     */
    public BillingDocument loadForUpdate(String enterpriseId, DocumentType type, UUID id, Long expectedVersion) {
        BillingDocument document = repository.findByIdForUpdate(enterpriseId, id)
                .orElseThrow(() -> new BillingException(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND,
                        "Documento no encontrado"));
        document.ensureType(type);
        document.ensureVersion(expectedVersion);
        return document;
    }

    /** Carga y verifica la version esperada ({@code null} omite la verificacion). */
    public BillingDocument load(String enterpriseId, DocumentType type, UUID id, Long expectedVersion) {
        BillingDocument document = load(enterpriseId, type, id);
        document.ensureVersion(expectedVersion);
        return document;
    }
}
