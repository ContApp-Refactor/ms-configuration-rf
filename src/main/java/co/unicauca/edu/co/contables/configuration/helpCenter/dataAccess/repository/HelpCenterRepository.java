package co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.DocumentModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HelpCenterRepository extends JpaRepository<HelpCenterEntity, Long> {

    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);

    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);

    Page<HelpCenterEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    Optional<HelpCenterEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);

    List<HelpCenterEntity> findAllByModuleAndIdEnterprise(DocumentModule module, String idEnterprise);

    long countByIdEnterprise(String idEnterprise);


    @Query("SELECT h FROM HelpCenterEntity h WHERE h.idEnterprise = :idEnterprise " +
           "AND (LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(h.description AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<HelpCenterEntity> searchByEnterpriseAndText(
            @Param("idEnterprise") String idEnterprise, 
            @Param("search") String search, 
            Pageable pageable);

    @Query("SELECT COUNT(h) FROM HelpCenterEntity h WHERE h.idEnterprise = :idEnterprise " +
           "AND (LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(h.description AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countByEnterpriseAndText(
            @Param("idEnterprise") String idEnterprise, 
            @Param("search") String search);
}
