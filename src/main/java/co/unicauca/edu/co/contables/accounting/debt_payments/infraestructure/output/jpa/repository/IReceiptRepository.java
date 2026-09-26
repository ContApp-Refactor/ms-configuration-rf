package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @brief Repository interface for managing Receipt entities.
 * This interface extends JpaRepository to provide CRUD operations and custom queries for ReceiptEntity.
 */
@Repository
public interface IReceiptRepository extends JpaRepository<ReceiptEntity, Long> {

    Optional<ReceiptEntity> findById(Long id);

    List<ReceiptEntity> findByThirdPartyIdAndEnterpriseId(Long thirdPartyId, String enterpriseId);

    List<ReceiptEntity> findAllByEnterpriseId(String enterpriseId);

    boolean existsById(Long id);

    /**
     * Finds all receipts that contain a detail associated with a specific invoice ID.
     * @param invoiceId The ID of the invoice.
     * @return A list of matching ReceiptEntity objects.
     */
    @Query("SELECT r FROM ReceiptEntity r JOIN r.details d WHERE d.invoiceId = :invoiceId")
    List<ReceiptEntity> findByInvoiceId(@Param("invoiceId") Long invoiceId);
}
