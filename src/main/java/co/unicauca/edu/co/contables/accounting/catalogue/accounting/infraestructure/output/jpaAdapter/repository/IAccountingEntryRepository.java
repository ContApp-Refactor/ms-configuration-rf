package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingEntryEntity;

public interface IAccountingEntryRepository extends JpaRepository<AccountingEntryEntity, Long>{
    
    Optional<AccountingEntryEntity> findBySourceDocumentId(Long sourceDocumentId);

    // Para buscar asientos por ID. Aunque JpaRepository ya tiene findById,
    // este podría incluir un JOIN FETCH si queremos cargar los movimientos de una vez.
    @Query("SELECT ae FROM AccountingEntryEntity ae JOIN FETCH ae.movements WHERE ae.id = :id")
    Optional<AccountingEntryEntity> findByIdWithMovements(@Param("id") Long id);

    Optional<AccountingEntryEntity> findBySourceDocumentIdAndType(Long sourceDocumentId, String type);
    boolean existsBySourceDocumentIdAndType(Long sourceDocumentId, String type);

    
}
