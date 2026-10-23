package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.input.DeleteDocumentDraftUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.Objects;
import java.util.UUID;

/** Elimina fisicamente un documento en BORRADOR (DRF-5, RN-07). Cualquier otro estado es 409. */
public class BillingDraftDeletionService implements DeleteDocumentDraftUseCase {

    private final BillingDocumentRepositoryPort repository;
    private final BillingDocumentLoader loader;

    public BillingDraftDeletionService(BillingDocumentRepositoryPort repository, BillingDocumentLoader loader) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    @Override
    public void delete(String enterpriseId, DocumentType type, UUID id, Long version) {
        BillingDocument document = loader.load(enterpriseId, type, id, version);
        document.assertDeletable();
        repository.delete(document);
    }
}
