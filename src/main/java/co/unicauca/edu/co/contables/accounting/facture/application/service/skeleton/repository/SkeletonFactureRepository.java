package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;

@Repository
public interface SkeletonFactureRepository extends JpaRepository<SkeletonFacture, Long> {
    
    Optional<SkeletonFacture> findByFactCode(Long factCode);
    List<SkeletonFacture> findByEntIdAndFactureType(String entId, SkeletonFactureType factureType);
    
    boolean existsByFactCode(Long factCode);
    
    @Query("SELECT f FROM SkeletonFacture f LEFT JOIN FETCH f.products WHERE f.id = :id")
    Optional<SkeletonFacture> findByIdWithProducts(Long id);
    
    @Query("SELECT f FROM SkeletonFacture f LEFT JOIN FETCH f.products WHERE f.factCode = :factCode")
    Optional<SkeletonFacture> findByFactCodeWithProducts(Long factCode);
}
