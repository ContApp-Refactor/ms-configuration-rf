package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterpriseCountryRepository extends JpaRepository<CountryEntity, Long> {
}
