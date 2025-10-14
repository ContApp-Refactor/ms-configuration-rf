package co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.DocumentModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HelpCenterRepository extends JpaRepository<HelpCenterEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<HelpCenterEntity> findAllByModule(DocumentModule module);

    /**
     * Busca registros de ayuda por término de búsqueda en nombre módulo, nombre o descripción
     * @param search Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de registros que coinciden en nombre módulo, nombre o descripción
     */
    @Query("SELECT h FROM HelpCenterEntity h " +
           "WHERE LOWER(CAST(h.module AS string)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<HelpCenterEntity> searchByText(
            @Param("search") String search, 
            Pageable pageable);

    /**
     * Cuenta registros de ayuda por término de búsqueda en nombre módulo, nombre o descripción
     * @param search Término de búsqueda
     * @return Número total de registros que coinciden
     */
    @Query("SELECT COUNT(h) FROM HelpCenterEntity h " +
           "WHERE LOWER(CAST(h.module AS string)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    long countByText(@Param("search") String search);
}
