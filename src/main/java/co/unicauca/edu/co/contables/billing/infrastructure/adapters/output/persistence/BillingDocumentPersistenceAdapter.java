package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.InvoiceReturnAggregate;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.mapper.BillingDocumentPersistenceMapper;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingDocumentJpaRepository;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingDocumentLineJpaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Persistencia JPA de documentos (puerto {@link BillingDocumentRepositoryPort}). Debe ejecutarse
 * dentro de una transaccion del llamador (los adaptadores de entrada transaccionales): el mapeo de
 * las lineas perezosas necesita sesion abierta (open-in-view=false).
 *
 * <p>El aislamiento por tenant es de Hibernate ({@code @TenantId}); la empresa se pasa y filtra
 * explicitamente. Control optimista: la version del documento debe coincidir con la persistida; si
 * no (o si el documento ya no existe / no es visible) se lanza
 * {@link BillingErrorCode#BILLING_CONCURRENT_MODIFICATION}.
 */
@Component("billingDocumentPersistenceAdapter")
public class BillingDocumentPersistenceAdapter implements BillingDocumentRepositoryPort {

    private final BillingDocumentJpaRepository repository;
    private final BillingDocumentLineJpaRepository lineRepository;
    private final BillingDocumentPersistenceMapper mapper;

    public BillingDocumentPersistenceAdapter(BillingDocumentJpaRepository repository,
            BillingDocumentLineJpaRepository lineRepository, BillingDocumentPersistenceMapper mapper) {
        this.repository = repository;
        this.lineRepository = lineRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<BillingDocument> findById(String enterpriseId, UUID id) {
        return repository.findByIdAndEnterpriseId(id, enterpriseId).map(mapper::toDomain);
    }

    /**
     * Bloquea la fila (PESSIMISTIC_WRITE, espera ~3 s). Si el bloqueo no se obtiene, la excepcion de
     * Spring/Hibernate ({@code PessimisticLockingFailureException} y afines) sube tal cual y el
     * advice REST la traduce a 503 BILLING_SEQUENCE_BUSY.
     */
    @Override
    public Optional<BillingDocument> findByIdForUpdate(String enterpriseId, UUID id) {
        return repository.findForUpdate(id, enterpriseId).map(mapper::toDomain);
    }

    @Override
    public boolean existsReturnInStatus(String enterpriseId, UUID originalDocumentId, DocumentStatus status) {
        return repository.existsByEnterpriseIdAndOriginalDocumentIdAndStatus(
                enterpriseId, originalDocumentId, status.name());
    }

    @Override
    public List<BillingDocumentSummary> findSummaries(String enterpriseId, DocumentType type, DocumentStatus status,
            UUID originalDocumentId) {
        String typeName = type.name();
        List<BillingDocumentEntity> rows;
        if (status == null && originalDocumentId == null) {
            rows = repository.findByEnterpriseIdAndDocumentTypeOrderByUpdatedAtDesc(enterpriseId, typeName);
        } else if (originalDocumentId == null) {
            rows = repository.findByEnterpriseIdAndDocumentTypeAndStatusOrderByUpdatedAtDesc(
                    enterpriseId, typeName, status.name());
        } else if (status == null) {
            rows = repository.findByEnterpriseIdAndDocumentTypeAndOriginalDocumentIdOrderByUpdatedAtDesc(
                    enterpriseId, typeName, originalDocumentId);
        } else {
            rows = repository.findByEnterpriseIdAndDocumentTypeAndStatusAndOriginalDocumentIdOrderByUpdatedAtDesc(
                    enterpriseId, typeName, status.name(), originalDocumentId);
        }
        return rows.stream().map(mapper::toSummary).toList();
    }

    @Override
    public List<BillingDocument> findReturnsOf(String enterpriseId, UUID originalDocumentId) {
        return repository.findByEnterpriseIdAndOriginalDocumentIdOrderByCreatedAtAsc(enterpriseId,
                originalDocumentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<BillingDocumentSummary> findSummariesByIds(String enterpriseId, Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findByEnterpriseIdAndIdInOrderByUpdatedAtDesc(enterpriseId, ids).stream()
                .map(mapper::toSummary).toList();
    }

    /**
     * Combina dos consultas (totales por un lado, cantidades por otro) para no multiplicar los
     * totales por el numero de lineas. Solo cuenta devoluciones EMITIDA (RET-3, RET-11).
     */
    @Override
    public List<InvoiceReturnAggregate> aggregateEmittedReturns(String enterpriseId, DocumentType returnType) {
        String typeName = returnType.name();
        Map<UUID, BigDecimal> quantities = new HashMap<>();
        for (Object[] row : lineRepository.sumEmittedReturnQuantitiesByOriginal(enterpriseId, typeName)) {
            quantities.put((UUID) row[0], toBigDecimal(row[1]));
        }
        List<InvoiceReturnAggregate> result = new ArrayList<>();
        for (Object[] row : repository.sumEmittedReturnsByOriginal(enterpriseId, typeName)) {
            UUID originalId = (UUID) row[0];
            result.add(new InvoiceReturnAggregate(originalId, ((Number) row[1]).longValue(),
                    Money.of(toBigDecimal(row[2])), quantities.getOrDefault(originalId, BigDecimal.ZERO)));
        }
        return result;
    }

    @Override
    public Map<UUID, BigDecimal> soldQuantities(String enterpriseId, Collection<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, BigDecimal> sold = new HashMap<>();
        for (Object[] row : lineRepository.sumQuantitiesByDocument(enterpriseId, documentIds)) {
            sold.put((UUID) row[0], toBigDecimal(row[1]));
        }
        return sold;
    }

    @Override
    public BillingDocument save(BillingDocument document) {
        Optional<BillingDocumentEntity> existing =
                repository.findByIdAndEnterpriseId(document.id(), document.enterpriseId());
        BillingDocumentEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            if (document.version() == null || !document.version().equals(entity.getVersion())) {
                throw concurrentModification();
            }
            mapper.apply(document, entity);
        } else {
            if (document.version() != null) {
                // Tenia version, luego ya fue persistido: ahora no existe o no es visible para este tenant.
                throw concurrentModification();
            }
            entity = mapper.newEntity(document);
        }
        try {
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException ex) {
            throw concurrentModification();
        }
    }

    @Override
    public void delete(BillingDocument document) {
        repository.findByIdAndEnterpriseId(document.id(), document.enterpriseId()).ifPresent(entity -> {
            repository.delete(entity);
            repository.flush();
        });
    }

    /** Las sumas JPQL llegan como BigDecimal; se tolera cualquier Number por portabilidad entre motores. */
    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
    }

    private static BillingException concurrentModification() {
        return new BillingException(BillingErrorCode.BILLING_CONCURRENT_MODIFICATION,
                "El documento fue modificado o eliminado por otro usuario");
    }
}
