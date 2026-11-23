package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}
