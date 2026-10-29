package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio JPA de documentos. El filtro por tenant lo aplica Hibernate ({@code @TenantId}) en
 * todas las consultas; la empresa se filtra explicitamente. Los listados usan cuatro metodos
 * derivados (en vez de una consulta con parametros opcionales nulos) para que el tipado de los
 * parametros sea inequivoco en PostgreSQL.
 */
public interface BillingDocumentJpaRepository extends JpaRepository<BillingDocumentEntity, UUID> {

    Optional<BillingDocumentEntity> findByIdAndEnterpriseId(UUID id, String enterpriseId);

    /**
     * {@code SELECT ... FOR UPDATE} del documento con espera maxima de 3 s (D12). Sin join fetch:
     * las lineas perezosas se cargan despues, ya con la fila bloqueada. El tenant lo filtra
     * Hibernate ({@code @TenantId}).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select d from BillingDocumentEntity d where d.id = :id and d.enterpriseId = :enterpriseId")
    Optional<BillingDocumentEntity> findForUpdate(@Param("id") UUID id, @Param("enterpriseId") String enterpriseId);

    boolean existsByEnterpriseIdAndOriginalDocumentIdAndStatus(String enterpriseId, UUID originalDocumentId,
            String status);

    /** Devoluciones (cualquier estado) de una factura original, de la mas antigua a la mas nueva. */
    List<BillingDocumentEntity> findByEnterpriseIdAndOriginalDocumentIdOrderByCreatedAtAsc(
            String enterpriseId, UUID originalDocumentId);

    List<BillingDocumentEntity> findByEnterpriseIdAndIdInOrderByUpdatedAtDesc(
            String enterpriseId, Collection<UUID> ids);

    /**
     * Por factura original: numero y suma de totales de sus devoluciones EMITIDA del tipo dado.
     * Filas {@code [UUID originalDocumentId, Long count, BigDecimal sumTotal]}. No une lineas (la
     * suma de totales se duplicaria por linea); las cantidades las suma
     * {@link BillingDocumentLineJpaRepository}.
     */
    @Query("select d.originalDocumentId, count(d), sum(d.total) from BillingDocumentEntity d "
            + "where d.enterpriseId = :enterpriseId and d.documentType = :documentType "
            + "and d.status = 'EMITIDA' and d.originalDocumentId is not null group by d.originalDocumentId")
    List<Object[]> sumEmittedReturnsByOriginal(@Param("enterpriseId") String enterpriseId,
            @Param("documentType") String documentType);

    List<BillingDocumentEntity> findByEnterpriseIdAndDocumentTypeOrderByUpdatedAtDesc(
            String enterpriseId, String documentType);

    List<BillingDocumentEntity> findByEnterpriseIdAndDocumentTypeAndStatusOrderByUpdatedAtDesc(
            String enterpriseId, String documentType, String status);

    List<BillingDocumentEntity> findByEnterpriseIdAndDocumentTypeAndOriginalDocumentIdOrderByUpdatedAtDesc(
            String enterpriseId, String documentType, UUID originalDocumentId);

    List<BillingDocumentEntity> findByEnterpriseIdAndDocumentTypeAndStatusAndOriginalDocumentIdOrderByUpdatedAtDesc(
            String enterpriseId, String documentType, String status, UUID originalDocumentId);
}
