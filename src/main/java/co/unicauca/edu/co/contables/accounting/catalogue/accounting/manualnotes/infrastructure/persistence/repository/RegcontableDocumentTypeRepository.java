package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.repository;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity.DocumentTypeEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegcontableDocumentTypeRepository extends JpaRepository<DocumentTypeEntity, Long> {

    Optional<DocumentTypeEntity> findByCodeAndEnterpriseId(String code, String enterpriseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from RegcontableDocumentType d where d.code = :code and d.enterpriseId = :enterpriseId")
    Optional<DocumentTypeEntity> findByCodeWithLock(@Param("code") String code,
                                                    @Param("enterpriseId") String enterpriseId);
}
