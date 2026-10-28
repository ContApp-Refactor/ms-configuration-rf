package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.repository;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity.AccountingNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public interface AccountingNoteRepository extends JpaRepository<AccountingNoteEntity, Long> {

    @Query("select n from AccountingNoteEntity n left join fetch n.details " +
            "where n.id = :id and n.enterpriseId = :enterpriseId")
    Optional<AccountingNoteEntity> findByIdWithDetails(@Param("id") Long id,
                                                       @Param("enterpriseId") String enterpriseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from AccountingNoteEntity n where n.id = :id")
    Optional<AccountingNoteEntity> findByIdForUpdate(@Param("id") Long id);

    List<AccountingNoteEntity> findAllByEnterpriseId(String enterpriseId);

    boolean existsByOriginModuleAndOriginId(String originModule, String originId);

    @Query("select n from AccountingNoteEntity n left join fetch n.details " +
            "where n.originModule = :originModule and n.originId = :originId")
    Optional<AccountingNoteEntity> findByOrigin(@Param("originModule") String originModule,
                                                @Param("originId") String originId);

    @Query(value = """
            select * from (
                select n.id as "noteId", n.number as "noteNumber", n.date as "movementDate",
                       n.document_type_code as "documentTypeCode", n.concept as "concept",
                       n.origin_module as "originModule", n.origin_id as "originId", n.status as "status",
                       d.account_code as "accountCode", d.third_party_id as "thirdPartyId",
                       d.cost_center_code as "costCenterCode", d.description as "description",
                       d.debit as "debit", d.credit as "credit"
                from regcontable_accounting_note_details d
                join regcontable_accounting_notes n on n.id = d.accounting_note_id
                where n.enterprise_id = :enterpriseId
                  and n.status = 'POSTED'
                  and d.account_code = :accountCode
                  and n.date between :startDate and :endDate
                  and (:thirdPartyId is null or d.third_party_id = :thirdPartyId)
                  and (:costCenterCode is null or d.cost_center_code = :costCenterCode)
                  and (:originModule is null or n.origin_module = :originModule)

                union all

                select e.id as "noteId", e.code as "noteNumber", e.date as "movementDate",
                       e.type as "documentTypeCode", e.description as "concept",
                       e.type as "originModule", cast(e.source_document_id as varchar) as "originId",
                       cast(e.status as varchar) as "status", a.code as "accountCode",
                       cast(m.third_party_id as varchar) as "thirdPartyId",
                       cast(e.center_cost_id as varchar) as "costCenterCode",
                       m.description as "description", m.debit as "debit", m.credit as "credit"
                from accounting_movements m
                join accounting_entries e on e.id = m.accounting_entry_id
                join Account a on a.id = m.account_id and a.id_enterprise = e.id_enterprise
                where e.id_enterprise = :enterpriseId
                  and a.code = :accountCode
                  and e.date between :startDate and :endDate
                  and (:thirdPartyId is null or cast(m.third_party_id as varchar) = :thirdPartyId)
                  and (:costCenterCode is null or cast(e.center_cost_id as varchar) = :costCenterCode)
                  and (:originModule is null or e.type = :originModule)
            ) movement_rows
            order by "movementDate", "noteId"
            """, nativeQuery = true)
    List<MovementProjection> findMovements(@Param("accountCode") String accountCode,
                                           @Param("enterpriseId") String enterpriseId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("thirdPartyId") String thirdPartyId,
                                           @Param("costCenterCode") String costCenterCode,
                                           @Param("originModule") String originModule);

    @Query(value = """
            select coalesce(sum(movement_rows.debit - movement_rows.credit), 0)
            from (
                select d.debit as debit, d.credit as credit
                from regcontable_accounting_note_details d
                join regcontable_accounting_notes n on n.id = d.accounting_note_id
                where n.enterprise_id = :enterpriseId
                  and d.account_code = :accountCode
                  and n.date < :startDate
                  and n.status = 'POSTED'
                  and (:thirdPartyId is null or d.third_party_id = :thirdPartyId)
                  and (:costCenterCode is null or d.cost_center_code = :costCenterCode)
                  and (:originModule is null or n.origin_module = :originModule)

                union all

                select m.debit as debit, m.credit as credit
                from accounting_movements m
                join accounting_entries e on e.id = m.accounting_entry_id
                join Account a on a.id = m.account_id and a.id_enterprise = e.id_enterprise
                where e.id_enterprise = :enterpriseId
                  and a.code = :accountCode
                  and e.date < :startDate
                  and e.status = 'ACTIVE'
                  and (:thirdPartyId is null or cast(m.third_party_id as varchar) = :thirdPartyId)
                  and (:costCenterCode is null or cast(e.center_cost_id as varchar) = :costCenterCode)
                  and (:originModule is null or e.type = :originModule)
            ) movement_rows
            """, nativeQuery = true)
    BigDecimal findPreviousBalance(@Param("accountCode") String accountCode,
                                   @Param("enterpriseId") String enterpriseId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("thirdPartyId") String thirdPartyId,
                                   @Param("costCenterCode") String costCenterCode,
                                   @Param("originModule") String originModule);

    interface MovementProjection {
        Long getNoteId(); String getNoteNumber(); LocalDate getMovementDate();
        String getDocumentTypeCode(); String getConcept(); String getOriginModule();
        String getOriginId(); String getStatus(); String getAccountCode();
        String getThirdPartyId(); String getCostCenterCode(); String getDescription();
        BigDecimal getDebit(); BigDecimal getCredit();
    }
}
