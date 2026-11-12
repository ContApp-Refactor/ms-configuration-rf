package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.billing.application.ports.input.GetDocumentUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.GetReturnAvailabilityUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListDocumentReturnsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListDocumentsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListInvoicesWithReturnsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.InvoiceWithReturnsSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.ReturnSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ThirdPartyPort;
import co.unicauca.edu.co.contables.billing.application.service.BillingDocumentLoader;
import co.unicauca.edu.co.contables.billing.application.service.BillingDocumentQueryService;
import co.unicauca.edu.co.contables.billing.application.service.BillingReferenceResolver;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de consulta (D3): transaccion de solo lectura que mantiene abierta la sesion para
 * mapear las lineas perezosas (open-in-view=false). Refrescar el catalogo de un borrador al leerlo
 * (DRF-8) ocurre solo en memoria: nada se persiste. Incluye las consultas derivadas de
 * devoluciones (disponibilidad, devoluciones de una factura, facturas con devoluciones).
 */
@Component("billingTransactionalQueryUseCases")
public class BillingTransactionalQueryUseCases implements GetDocumentUseCase, ListDocumentsUseCase,
        GetReturnAvailabilityUseCase, ListDocumentReturnsUseCase, ListInvoicesWithReturnsUseCase {

    private final BillingDocumentQueryService queryService;

    public BillingTransactionalQueryUseCases(BillingDocumentRepositoryPort repository, ThirdPartyPort thirdParties,
            ProductPort products, EnterprisePort enterprises) {
        BillingDocumentLoader loader = new BillingDocumentLoader(repository);
        BillingReferenceResolver resolver = new BillingReferenceResolver(thirdParties, products, enterprises);
        this.queryService = new BillingDocumentQueryService(repository, loader, resolver);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingDocumentView get(String enterpriseId, DocumentType type, UUID id) {
        return queryService.get(enterpriseId, type, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingDocumentSummary> list(String enterpriseId, DocumentType type, DocumentStatus status,
            UUID originalDocumentId) {
        return queryService.list(enterpriseId, type, status, originalDocumentId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnAvailability availability(String enterpriseId, DocumentType invoiceType, UUID invoiceId) {
        return queryService.availability(enterpriseId, invoiceType, invoiceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnSummary> returnsOf(String enterpriseId, DocumentType invoiceType, UUID invoiceId) {
        return queryService.returnsOf(enterpriseId, invoiceType, invoiceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceWithReturnsSummary> list(String enterpriseId, DocumentType invoiceType) {
        return queryService.list(enterpriseId, invoiceType);
    }
}
