package co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @brief Repositorio para registros del centro de ayuda
 *
 * Repositorio JPA que proporciona operaciones de acceso a datos para entidades
 * del centro de ayuda, incluyendo consultas personalizadas y búsquedas.
 */
public interface HelpCenterRepository extends JpaRepository<HelpCenterEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<HelpCenterEntity> findAllByModuleAndStatus(DocumentModule module, Boolean status);

    /**
     * @brief Busca registros de ayuda por término de búsqueda en ID módulo, nombre módulo o nombre
     * @param search Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de registros que coinciden en ID módulo, nombre módulo o nombre
     */
    @Query("SELECT h FROM HelpCenterEntity h " +
           "WHERE LOWER(CAST(h.module AS string)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(h.moduleId AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<HelpCenterEntity> searchByText(
            @Param("search") String search, 
            Pageable pageable);

    /**
     * @brief Cuenta registros de ayuda por término de búsqueda en ID módulo, nombre módulo o nombre
     * @param search Término de búsqueda
     * @return Número total de registros que coinciden
     */
    @Query("SELECT COUNT(h) FROM HelpCenterEntity h " +
           "WHERE LOWER(CAST(h.module AS string)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(h.moduleId AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    long countByText(@Param("search") String search);
}
