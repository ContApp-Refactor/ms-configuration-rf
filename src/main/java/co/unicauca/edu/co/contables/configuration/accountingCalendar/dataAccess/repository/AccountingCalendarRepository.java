package co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

public interface AccountingCalendarRepository extends JpaRepository<AccountingCalendarEntity, Long> {

    boolean existsByIdEnterpriseAndDate(String idEnterprise, LocalDate date);

    Optional<AccountingCalendarEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);

    List<AccountingCalendarEntity> findAllByIdEnterpriseAndDateBetween(
            String idEnterprise, LocalDate startDate, LocalDate endDate);

    List<AccountingCalendarEntity> findAllByIdEnterpriseAndDateBetweenOrderByDateAsc(
            String idEnterprise, LocalDate startDate, LocalDate endDate);

    long deleteByIdEnterpriseAndDateBetween(
            String idEnterprise, LocalDate startDate, LocalDate endDate);

}


