package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.InvoiceReturnAggregate;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de documentos. El aislamiento por tenant lo resuelve el adaptador (TEN-1); el
 * alcance por empresa se pasa explicitamente.
 */
public interface BillingDocumentRepositoryPort {

    /** Documento del tenant y empresa actuales, o vacio si no existe o es de otro ambito. */
    Optional<BillingDocument> findById(String enterpriseId, UUID id);

    /**
     * Igual que {@link #findById} pero bloquea la fila del documento hasta el fin de la transaccion
     * ({@code SELECT ... FOR UPDATE}, espera maxima ~3 s, D12): serializa la anulacion contra la
     * emision de devoluciones del mismo original. Requiere una transaccion activa.
     */
    Optional<BillingDocument> findByIdForUpdate(String enterpriseId, UUID id);

    /**
     * Indica si existe algun documento cuyo original sea {@code originalDocumentId} en el estado
     * dado (ANL-2: devoluciones EMITIDA bloquean la anulacion de la factura).
     */
    boolean existsReturnInStatus(String enterpriseId, UUID originalDocumentId, DocumentStatus status);

    /** Resumenes ordenados por updatedAt descendente; {@code status} y {@code originalDocumentId} opcionales. */
    List<BillingDocumentSummary> findSummaries(String enterpriseId, DocumentType type, DocumentStatus status,
            UUID originalDocumentId);

    /**
     * Todas las devoluciones (cualquier estado) cuya original es {@code originalDocumentId}, con
     * sus lineas. No bloquea; quien calcula disponibilidad cuenta solo las EMITIDA (RET-3).
     */
    List<BillingDocument> findReturnsOf(String enterpriseId, UUID originalDocumentId);

    /** Resumenes de los documentos dados (de la empresa), ordenados por updatedAt descendente. */
    List<BillingDocumentSummary> findSummariesByIds(String enterpriseId, Collection<UUID> ids);

    /**
     * Por factura original: numero y suma de totales de sus devoluciones EMITIDA del tipo dado y suma
     * de cantidades de linea (RET-11). Solo aparecen las facturas con al menos una devolucion EMITIDA.
     */
    List<InvoiceReturnAggregate> aggregateEmittedReturns(String enterpriseId, DocumentType returnType);

    /** Suma de las cantidades de linea de cada documento pedido; los inexistentes no aparecen. */
    Map<UUID, BigDecimal> soldQuantities(String enterpriseId, Collection<UUID> documentIds);

    /** Inserta o actualiza (control optimista por version); devuelve el estado persistido. */
    BillingDocument save(BillingDocument document);

    void delete(BillingDocument document);
}
