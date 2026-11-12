package co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.entity.TagEntity;

public interface ITagRepository extends JpaRepository<TagEntity,Long>{

    List<TagEntity> findByEnterpriseId(String enterpriseId);

    /** Copia de seguridad: lectura paginada de los tags de la empresa. */
    org.springframework.data.domain.Page<TagEntity> findAllByEnterpriseId(
            String enterpriseId, org.springframework.data.domain.Pageable pageable);

    long countByEnterpriseId(String enterpriseId);

    Optional<TagEntity> findByIdAndEnterpriseId(Long id, String enterpriseId);
    
}
