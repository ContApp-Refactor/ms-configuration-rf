package co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;

public interface DocumentClassRepository extends JpaRepository<DocumentClassEntity, Long> {
    boolean existsByNameAndIdEnterpriseAndIsDeletedFalse(String name, String idEnterprise);

    boolean existsByNameAndIdEnterpriseAndIdNotAndIsDeletedFalse(String name, String idEnterprise, Long id);

    Page<DocumentClassEntity> findAllByIdEnterpriseAndIsDeletedFalse(String idEnterprise, Pageable pageable);

    Page<DocumentClassEntity> findAllByIdEnterpriseAndStatusAndIsDeletedFalse(String idEnterprise, Boolean status, Pageable pageable);

    Optional<DocumentClassEntity> findByIdAndIdEnterpriseAndIsDeletedFalse(Long id, String idEnterprise);

    Optional<DocumentClassEntity> findByIdAndIdEnterpriseAndStatusAndIsDeletedFalse(Long id, String idEnterprise, Boolean status);

    /**
     * Cuenta el total de clases de documento por empresa (no eliminadas)
     * @param idEnterprise ID de la empresa
     * @return Número total de clases de documento
     */
    long countByIdEnterpriseAndIsDeletedFalse(String idEnterprise);

    /**
     * Cuenta el total de clases de documento filtradas por estado (no eliminadas)
     * @param idEnterprise ID de la empresa
     * @param status Estado de la clase de documento
     * @return Número total de clases de documento con el estado especificado
     */
    long countByIdEnterpriseAndStatusAndIsDeletedFalse(String idEnterprise, Boolean status);
}


