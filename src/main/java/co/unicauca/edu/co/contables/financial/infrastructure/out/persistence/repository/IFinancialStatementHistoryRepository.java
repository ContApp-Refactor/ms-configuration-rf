package co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.repository;

import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFinancialStatementHistoryRepository extends JpaRepository<FinancialStatementHistoryEntity, Long> {

    Page<FinancialStatementHistoryEntity> findByFinancialStatement_EntId(String entId, Pageable pageable);
}