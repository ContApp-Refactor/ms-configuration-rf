package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.PortfolioWriteOffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @brief Repository interface for managing Portfolio Write-Off entities.
 *        This interface extends JpaRepository to provide CRUD operations for
 *        PortfolioWriteOffEntity.
 */
public interface IPortfolioWriteOffRepository extends JpaRepository<PortfolioWriteOffEntity, Long> {
    /**
     * Finds all portfolio write-offs associated with a specific enterprise ID.
     * 
     * @param enterpriseId The ID of the enterprise.
     * @return A list of portfolio write-off entities.
     */
    List<PortfolioWriteOffEntity> findByEnterpriseId(String enterpriseId);

    /**
     * Finds all confirmed or voided portfolio write-offs associated with a specific
     * enterprise ID.
     * 
     * @param enterpriseId The ID of the enterprise.
     * @return A list of portfolio write-off entities.
     */
    @Query("""
                SELECT p
                FROM PortfolioWriteOffEntity p
                WHERE p.enterpriseId = :enterpriseId
                AND p.status IN ('CONFIRMED', 'VOIDED')
            """)
    List<PortfolioWriteOffEntity> findConfirmedOrVoidedByEnterpriseId(
            @Param("enterpriseId") String enterpriseId);
}
