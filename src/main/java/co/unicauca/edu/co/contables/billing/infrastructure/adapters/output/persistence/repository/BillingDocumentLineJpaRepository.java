package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentLineEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Agregados de lineas para las consultas de devoluciones (RET-11). Solo lectura: las lineas se
 * escriben a traves del agregado (cascade desde el documento). El tenant lo filtra Hibernate
 * ({@code @TenantId}) tambien en las uniones.
 */
public interface BillingDocumentLineJpaRepository extends JpaRepository<BillingDocumentLineEntity, Long> {

    /**
     * Por factura original: suma de cantidades de linea de sus devoluciones EMITIDA del tipo dado.
     * Filas {@code [UUID originalDocumentId, BigDecimal sumQuantity]}.
     */
    @Query("select d.originalDocumentId, sum(l.quantity) from BillingDocumentLineEntity l join l.document d "
            + "where d.enterpriseId = :enterpriseId and d.documentType = :documentType "
            + "and d.status = 'EMITIDA' and d.originalDocumentId is not null group by d.originalDocumentId")
    List<Object[]> sumEmittedReturnQuantitiesByOriginal(@Param("enterpriseId") String enterpriseId,
            @Param("documentType") String documentType);

    /** Suma de cantidades de linea por documento (de la empresa). Filas {@code [UUID id, BigDecimal sum]}. */
    @Query("select d.id, sum(l.quantity) from BillingDocumentLineEntity l join l.document d "
            + "where d.enterpriseId = :enterpriseId and d.id in :documentIds group by d.id")
    List<Object[]> sumQuantitiesByDocument(@Param("enterpriseId") String enterpriseId,
            @Param("documentIds") Collection<UUID> documentIds);
}
