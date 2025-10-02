package co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, Long> {

    boolean existsByPrefixAndIdEnterprise(String prefix, String idEnterprise);

    boolean existsByPrefixAndIdEnterpriseAndIdNot(String prefix, String idEnterprise, Long id);

    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);

    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);

    Page<DocumentTypeEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    Optional<DocumentTypeEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);
    
    Page<DocumentTypeEntity> findAllByModuleAndIdEnterprise(String module, String idEnterprise, Pageable pageable);

    // Método para verificar si una clase de documento está siendo usada por tipos
    boolean existsByDocumentClassId(Long documentClassId);

    /**
     * Cuenta el total de tipos de documento por empresa
     * @param idEnterprise ID de la empresa
     * @return Número total de tipos de documento
     */
    long countByIdEnterprise(String idEnterprise);

    /**
     * Cuenta el total de tipos de documento filtrados por módulo
     * @param module Módulo del tipo de documento
     * @param idEnterprise ID de la empresa
     * @return Número total de tipos de documento del módulo especificado
     */
    long countByModuleAndIdEnterprise(String module, String idEnterprise);

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
}


