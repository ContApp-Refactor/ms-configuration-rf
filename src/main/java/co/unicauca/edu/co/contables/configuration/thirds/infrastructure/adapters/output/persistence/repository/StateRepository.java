package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.StateEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.StateEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @brief Repositorio JPA para entidades de estados/departamentos con clave compuesta
 */
@Repository
public interface StateRepository extends JpaRepository<StateEntity, StateEntityId> {

    @Query("SELECT s FROM StateEntity s WHERE s.countryCode = :countryCode ORDER BY s.stateName")
    List<StateEntity> findByCountryCodeOrderByStateName(@Param("countryCode") String countryCode);

    @Query("SELECT COUNT(s) > 0 FROM StateEntity s WHERE s.stateCode = :stateCode AND s.countryCode = :countryCode")
    boolean existsByStateCodeAndCountryCode(@Param("stateCode") String stateCode, @Param("countryCode") String countryCode);

    /**
     * @brief Obtiene todos los estados activos sin filtrar por país
     * @details Optimizado para carga batch en exportaciones masivas
     * @return Lista de todos los estados ordenados por nombre
     */
    @Query("SELECT s FROM StateEntity s ORDER BY s.stateName")
    List<StateEntity> findAllStates();
}
