package co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.repository;

import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IFinancialStatementRepository extends JpaRepository<FinancialStatementEntity, Long> {

    Optional<FinancialStatementEntity> findByReportId(UUID reportId);
}