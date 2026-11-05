package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxLiabilityRepository extends JpaRepository<TaxLiabilityEntity, Long> {
}
