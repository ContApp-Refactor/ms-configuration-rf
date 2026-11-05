package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterpriseTypeRepository extends JpaRepository<EnterpriseTypeEntity, Long> {
}
