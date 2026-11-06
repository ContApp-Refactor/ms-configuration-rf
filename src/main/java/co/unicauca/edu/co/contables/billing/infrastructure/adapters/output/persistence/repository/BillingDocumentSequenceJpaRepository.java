package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentSequenceEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio de secuencias. El tenant lo filtra Hibernate ({@code @TenantId}); empresa y tipo se
 * pasan explicitamente. La consulta de bloqueo no usa join fetch (design §6).
 */
public interface BillingDocumentSequenceJpaRepository extends JpaRepository<BillingDocumentSequenceEntity, Long> {

    /** {@code SELECT ... FOR UPDATE} con espera maxima de 3 s (D11). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select s from BillingDocumentSequenceEntity s "
            + "where s.enterpriseId = :enterpriseId and s.documentType = :documentType")
    Optional<BillingDocumentSequenceEntity> findForUpdate(@Param("enterpriseId") String enterpriseId,
            @Param("documentType") String documentType);
}
