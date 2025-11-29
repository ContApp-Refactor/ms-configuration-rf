package co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

/**
 * @brief Repositorio para el acceso a datos del calendario contable
 *
 * Interfaz que proporciona operaciones de acceso a datos para la entidad AccountingCalendarEntity,
 * incluyendo consultas personalizadas para filtrar por empresa y rango de fechas.
 * Extiende JpaRepository para operaciones CRUD básicas.
 */
public interface AccountingCalendarRepository extends JpaRepository<AccountingCalendarEntity, Long> {

    boolean existsByIdEnterpriseAndDate(String idEnterprise, LocalDate date);

    Optional<AccountingCalendarEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);

    List<AccountingCalendarEntity> findAllByIdEnterpriseAndDateBetween(
            String idEnterprise, LocalDate startDate, LocalDate endDate);

    List<AccountingCalendarEntity> findAllByIdEnterpriseAndDateBetweenOrderByDateAsc(
            String idEnterprise, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT YEAR(ac.date) FROM AccountingCalendarEntity ac " +
           "WHERE ac.idEnterprise = :idEnterprise ORDER BY YEAR(ac.date)")
    List<Integer> findDistinctYearsByIdEnterprise(@Param("idEnterprise") String idEnterprise);

    long deleteByIdEnterpriseAndDateBetween(
            String idEnterprise, LocalDate startDate, LocalDate endDate);

}


