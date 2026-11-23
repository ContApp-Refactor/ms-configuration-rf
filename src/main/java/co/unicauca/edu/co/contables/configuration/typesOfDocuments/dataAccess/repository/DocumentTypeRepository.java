package co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** @brief Repositorio JPA para la gestión de tipos de documentos */
public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, Long> {

    boolean existsByPrefixAndIdEnterprise(String prefix, String idEnterprise);

    boolean existsByPrefixAndIdEnterpriseAndIdNot(String prefix, String idEnterprise, Long id);

    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);

    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);

    Page<DocumentTypeEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    Optional<DocumentTypeEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);
    
    List<DocumentTypeEntity> findAllByModuleAndIdEnterprise(String module, String idEnterprise);

    boolean existsByDocumentClassId(Long documentClassId);

    /**
     * Copia de seguridad: ids de tipos de la empresa cuya clase de documento no
     * es de la misma empresa (left join: el filtro de tenant también cuenta).
     */
    @org.springframework.data.jpa.repository.Query("SELECT t.id FROM DocumentTypeEntity t "
            + "LEFT JOIN t.documentClass c WHERE t.idEnterprise = :idEnterprise "
            + "AND (c IS NULL OR c.idEnterprise <> :idEnterprise)")
    List<Long> findIdsWithForeignDocumentClass(
            @org.springframework.data.repository.query.Param("idEnterprise") String idEnterprise);
   
    long countByIdEnterprise(String idEnterprise);

    /**
     * Busca tipos de documento por empresa y nombre (búsqueda parcial case-insensitive)
     * @param idEnterprise ID de la empresa
     * @param name Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de tipos de documento que coinciden con la búsqueda
     */
    Page<DocumentTypeEntity> findByIdEnterpriseAndNameContainingIgnoreCase(String idEnterprise, String name, Pageable pageable);

    /**
     * Cuenta tipos de documento por empresa y nombre (búsqueda parcial case-insensitive)
     * @param idEnterprise ID de la empresa
     * @param name Término de búsqueda
     * @return Número total de tipos de documento que coinciden con la búsqueda
     */
    long countByIdEnterpriseAndNameContainingIgnoreCase(String idEnterprise, String name);

    /**
     * Verifica si una clase de documento tiene tipos con registros contables (usageCount > 0)
     * @param documentClassId ID de la clase de documento
     * @param idEnterprise ID de la empresa
     * @return true si tiene tipos con registros contables, false en caso contrario
     */
    boolean existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(Long documentClassId, String idEnterprise, Integer usageCount);
}


