package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.InvoiceReplicaEntity;

@Repository("accountingInvoiceRepository")
public interface IInvoiceRepository extends JpaRepository<InvoiceReplicaEntity, Long> {
    /**
     * Encuentra todas las facturas de un cliente específico (ThirdParty)
     * cuyo saldo pendiente sea mayor a cero.
     *
     * @param thirdId El ID del cliente.
     * @param pendingValue El valor contra el que se compara el saldo pendiente (normalmente cero).
     * @return Una lista de entidades de facturas con saldo pendiente.
     */
    List<InvoiceReplicaEntity> findByThirdIdAndPendingValueGreaterThan(Long thirdId, Long pendingValue);

    /**
     * Busca todas las entidades de factura cuyos IDs están en la lista proporcionada.
     */
    List<InvoiceReplicaEntity> findByIdIn(List<Long> ids);

    Optional<InvoiceReplicaEntity> findByFactCode(Long factCode);

    /**
     * Actualiza el estado de una lista de facturas a WRITTEN_OFF de forma masiva.
     * También pone el valor pendiente a 0.
     */
    @Modifying
    @Query("UPDATE AccountingInvoiceReplicaEntity e SET e.status = 'WRITTEN_OFF', e.pendingValue = 0 WHERE e.id IN :ids")
    void writeOffInvoicesByIds(@Param("ids") List<Long> ids);

    /**
     * Buscar facturas por Id de la empresa y estado activo.
     */
    List<InvoiceReplicaEntity> findByEntIdAndStatus(String entId, InvoiceStatus status);

    // --- NUEVO MÉTODO PARA EL REPORTE DE CARTERA (NIVEL 1) ---
    /**
     * Calcula un resumen de cartera para una lista de clientes (thirdIds).
     * Agrupa todas las facturas con saldo pendiente para los clientes dados y calcula
     * la deuda total, la deuda vencida, la deuda por vencer y la fecha de vencimiento más antigua.
     *
     * @param thirdIds La lista de IDs de clientes a consultar.
     * @return Una lista de proyecciones ClientSummaryData con los datos calculados para cada cliente.
     */
    /**
     * Encuentra todas las facturas con saldo pendiente para una lista de clientes (thirdIds).
     * Spring Data JPA generará la consulta "SELECT ... WHERE pendingValue > 0 AND thirdId IN (...)".
     *
     * @param thirdIds La lista de IDs de clientes a buscar.
     * @return Una lista de todas las facturas que coinciden.
     */
    List<InvoiceReplicaEntity> findByPendingValueGreaterThanAndThirdIdIn(Long pendingValue, List<Long> thirdIds);
}
