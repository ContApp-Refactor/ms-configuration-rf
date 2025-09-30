package co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request.*;

import java.util.List;

public interface IAccountingCalendarService {

	AccountingCalendar create(AccountingCalendarCreateReq request);

	AccountingCalendar findById(Long id, String idEnterprise);

	void delete(Long id, String idEnterprise);

	List<AccountingCalendar> openMonthBatch(AccountingCalendarCreateMonthReq request);

	long deleteByMonth(AccountingCalendarDeleteMonthReq request);

	List<AccountingCalendar> openYearBatch(AccountingCalendarCreateYearReq request);

	long deleteByYear(AccountingCalendarDeleteYearReq request);

	List<AccountingCalendar> findAllByEnterpriseAndYear(String idEnterprise, int year);

	List<Integer> findExistingYearsByEnterprise(String idEnterprise);

}


