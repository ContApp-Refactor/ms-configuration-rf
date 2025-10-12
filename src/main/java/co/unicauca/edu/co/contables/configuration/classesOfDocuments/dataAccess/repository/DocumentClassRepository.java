package co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;

public interface DocumentClassRepository extends JpaRepository<DocumentClassEntity, Long> {
    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);

    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);

    Page<DocumentClassEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    Page<DocumentClassEntity> findAllByIdEnterpriseAndStatus(String idEnterprise, Boolean status, Pageable pageable);

    Optional<DocumentClassEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);

    Optional<DocumentClassEntity> findByIdAndIdEnterpriseAndStatus(Long id, String idEnterprise, Boolean status);

    /**
     * Cuenta el total de clases de documento por empresa
     * @param idEnterprise ID de la empresa
     * @return Número total de clases de documento
     */
    long countByIdEnterprise(String idEnterprise);

    /**
     * Cuenta el total de clases de documento filtradas por estado
     * @param idEnterprise ID de la empresa
     * @param status Estado de la clase de documento
     * @return Número total de clases de documento con el estado especificado
     */
    long countByIdEnterpriseAndStatus(String idEnterprise, Boolean status);

    /**
     * Busca clases de documento por empresa y nombre (búsqueda parcial case-insensitive)
     * @param idEnterprise ID de la empresa
     * @param name Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de clases de documento que coinciden con la búsqueda
     */
    Page<DocumentClassEntity> findByIdEnterpriseAndNameContainingIgnoreCase(String idEnterprise, String name, Pageable pageable);

    /**
     * Cuenta clases de documento por empresa y nombre (búsqueda parcial case-insensitive)
     * @param idEnterprise ID de la empresa
     * @param name Término de búsqueda
     * @return Número total de clases de documento que coinciden con la búsqueda
     */
    long countByIdEnterpriseAndNameContainingIgnoreCase(String idEnterprise, String name);
}


