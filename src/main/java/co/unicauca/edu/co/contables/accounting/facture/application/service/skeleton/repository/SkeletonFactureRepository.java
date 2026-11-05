package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;

@Repository
public interface SkeletonFactureRepository extends JpaRepository<SkeletonFacture, Long> {
    
    Optional<SkeletonFacture> findByFactCode(Long factCode);
    List<SkeletonFacture> findByEntIdAndFactureType(String entId, SkeletonFactureType factureType);
    
    boolean existsByFactCode(Long factCode);
    
    @Query("SELECT f FROM SkeletonFacture f LEFT JOIN FETCH f.products WHERE f.id = :id")
    Optional<SkeletonFacture> findByIdWithProducts(Long id);
    
    @Query("SELECT f FROM SkeletonFacture f LEFT JOIN FETCH f.products WHERE f.factCode = :factCode")
    Optional<SkeletonFacture> findByFactCodeWithProducts(Long factCode);

    /**
     * Precio unitario de la última compra vigente (no anulada) de un producto.
     *
     * Se usa para recalcular el costo del producto (HU4) cuando una compra se
     * crea, se edita o se anula. Las compras anuladas quedan excluidas.
     *
     * @param productId producto a consultar
     * @param factureType tipo de documento (PURCHASE)
     * @param voidedStatus estado que debe excluirse (VOIDED)
     * @param pageable limitado a un registro para quedarse con la más reciente
     * @return precios unitarios ordenados de la compra más reciente a la más antigua
     */
    @Query("""
            SELECT sp.unitPrice
            FROM SkeletonProduct sp
            JOIN sp.facture f
            WHERE sp.productId = :productId
              AND f.factureType = :factureType
              AND f.purchaseStatus <> :voidedStatus
            ORDER BY f.issueDate DESC, f.id DESC
            """)
    List<Double> findLastPurchaseUnitPrice(@Param("productId") Long productId,
                                           @Param("factureType") SkeletonFactureType factureType,
                                           @Param("voidedStatus") PurchaseInvoiceStatus voidedStatus,
                                           Pageable pageable);
}
