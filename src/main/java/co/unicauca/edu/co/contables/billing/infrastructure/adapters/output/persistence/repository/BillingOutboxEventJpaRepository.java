package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingOutboxEventEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio del outbox. Las consultas derivadas y JPQL pasan por el filtro {@code @TenantId} de
 * Hibernate (solo ven el tenant de la sesion); el reclamo del relay es SQL nativo a proposito porque
 * debe ver todos los tenants y el filtro lo ocultaria.
 */
public interface BillingOutboxEventJpaRepository extends JpaRepository<BillingOutboxEventEntity, Long> {

    /**
     * Reclamo del relay ENTRE TENANTS (SQL nativo: el filtro {@code @TenantId} no aplica). Cada
     * elemento es {@code [id, tenant_id]}; se lee por posicion para no depender de como el motor
     * normaliza los alias. Orden estable por vencimiento e id (indice {@code ix_billing_outbox_due}).
     */
    @Query(value = "select id, tenant_id from billing_outbox_events"
            + " where status = 'PENDING' and next_attempt_at <= :now"
            + " order by next_attempt_at, id limit :limit", nativeQuery = true)
    List<Object[]> findDueClaims(@Param("now") Instant now, @Param("limit") int limit);

    /**
     * {@code SELECT ... FOR UPDATE} de la fila con espera maxima de 3 s. Va por el filtro
     * {@code @TenantId}: solo encuentra la fila si la sesion se abrio con el tenant de la fila.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select e from BillingOutboxEventEntity e where e.id = :id")
    Optional<BillingOutboxEventEntity> findByIdForUpdate(@Param("id") Long id);
}
