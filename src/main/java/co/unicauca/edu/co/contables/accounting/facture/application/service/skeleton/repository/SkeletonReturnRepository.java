package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkeletonReturnRepository extends JpaRepository<SkeletonReturn, Long> {

    List<SkeletonReturn> findByOriginalFactCode(Long factCode);

    @Query("SELECT SUM(r.returnedQuantity) FROM SkeletonReturn r " +
            "WHERE r.originalFactCode = :factCode AND r.productId = :productId")
    Integer getTotalReturnedQuantity(Long factCode, Long productId);

    List<SkeletonReturn> findByOriginalFactCodeAndProductId(Long factCode, Long productId);

    Optional<SkeletonReturn> findTopByOriginalFactCodeOrderByIdDesc(Long factCode);
}
