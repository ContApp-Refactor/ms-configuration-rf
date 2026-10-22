package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxPayerTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxPayerTypeRepository extends JpaRepository<TaxPayerTypeEntity, Long> {
}
