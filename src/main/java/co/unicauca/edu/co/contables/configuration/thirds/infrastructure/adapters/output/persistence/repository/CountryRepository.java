package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @brief Repositorio JPA para entidades de países
 */
@Repository
public interface CountryRepository extends JpaRepository<CountryEntity, String> {

    @Query("SELECT c FROM CountryEntity c ORDER BY c.countryName")
    List<CountryEntity> findAllCountries();

    @Query("SELECT COUNT(c) > 0 FROM CountryEntity c WHERE c.countryCode = :countryCode")
    boolean existsByCountryCode(String countryCode);
}
