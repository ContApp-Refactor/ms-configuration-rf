package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.PersonTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonTypeRepository extends JpaRepository<PersonTypeEntity, Long> {
}
