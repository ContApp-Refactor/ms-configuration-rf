package co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.entity.TagEntity;

public interface ITagRepository extends JpaRepository<TagEntity,Long>{

    List<TagEntity> findByEnterpriseId(String enterpriseId);

    Optional<TagEntity> findByIdAndEnterpriseId(Long id, String enterpriseId);
    
}
