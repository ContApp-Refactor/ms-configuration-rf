package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.replicas;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IInvoiceRepository extends JpaRepository<InvoiceReplicaEntity, Long> {
    /**
     * Encuentra todas las facturas de un cliente específico (ThirdParty)
     * cuyo saldo pendiente sea mayor a cero.
     *
     * @param thirdId El ID del cliente.
     * @param pendingValue El valor contra el que se compara el saldo pendiente (normalmente cero).
     * @return Una lista de entidades de facturas con saldo pendiente.
     */
    List<InvoiceReplicaEntity> findByThirdIdAndEntIdAndPendingValueGreaterThan(Long thirdId, String entId, Long pendingValue);

    /**
     * Encuentra todas las facturas de un cliente específico (ThirdParty) con un estado específico.
     *
     * @param thirdId El ID del cliente.
     * @param status El estado de la factura.
     * @return Una lista de entidades de facturas con el estado especificado.
     */
    List<InvoiceReplicaEntity> findByThirdIdAndStatusAndEntId(Long thirdId, InvoiceStatus status, String entId);

    /**
     * Busca todas las entidades de factura cuyos IDs están en la lista proporcionada.
     */
    List<InvoiceReplicaEntity> findByIdIn(List<Long> ids);

    /**
     * Actualiza el estado de una lista de facturas a WRITTEN_OFF de forma masiva.
     * También pone el valor pendiente a 0.
     */
    @Modifying
    @Query("UPDATE InvoiceReplicaEntity e SET e.status = 'WRITTEN_OFF', e.pendingValue = 0 WHERE e.id IN :ids")
    void writeOffInvoicesByIds(@Param("ids") List<Long> ids);

    /**
     * Buscar facturas por Id de la empresa y estado activo.
     */
    List<InvoiceReplicaEntity> findByEntIdAndStatus(String entId, InvoiceStatus status);

    /**
     * Encuentra facturas de una empresa con un estado específico y cuya fecha de vencimiento
     * esté dentro de un rango (ej: Hoy y Hoy + 5 días).
     *
     * @param entId El ID de la empresa (Tenant).
     * @param status El estado (ej: PENDING).
     * @param start Inicio del rango (Inclusive).
     * @param end Fin del rango (Inclusive).
     * @return Lista de facturas próximas a vencer.
     */
    List<InvoiceReplicaEntity> findByEntIdAndStatusAndExpirationDateBetween(
        String entId, 
        InvoiceStatus status, 
        LocalDate start, 
        LocalDate end
    );

    /**
     * Find invoices by expiration date.
     * @param expirationDate The expiration date to filter invoices.
     * @return A list of InvoiceReplicaEntity that match the expiration date.
     */
    List<InvoiceReplicaEntity> findByExpirationDate(LocalDate expirationDate);
}
