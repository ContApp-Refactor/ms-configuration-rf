package co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @brief Servicio de dominio para el calendario contable
 *
 * Interfaz que define las operaciones de negocio para gestionar el calendario contable,
 * incluyendo creación, consulta, eliminación y operaciones por lotes (mes/año).
 */
public interface IAccountingCalendarService {

	/**
	 * @brief Crea un nuevo registro en el calendario contable
	 * @param request solicitud con los datos para crear el registro
	 * @return el calendario contable creado
	 */
	AccountingCalendar create(AccountingCalendarCreateReq request);

	/**
	 * @brief Busca un registro por ID y empresa
	 * @param id identificador del registro
	 * @param idEnterprise identificador de la empresa
	 * @return el calendario contable encontrado
	 */
	AccountingCalendar findById(Long id, String idEnterprise);

	/**
	 * @brief Elimina un registro por ID y empresa
	 * @param id identificador del registro
	 * @param idEnterprise identificador de la empresa
	 */
	void delete(Long id, String idEnterprise);

	/**
	 * @brief Abre un lote de meses en el calendario contable
	 * @param request solicitud con los datos para abrir el lote de meses
	 * @return lista de calendarios contables creados
	 */
	List<AccountingCalendar> openMonthBatch(AccountingCalendarCreateMonthReq request);

	/**
	 * @brief Elimina registros por mes
	 * @param request solicitud con los datos para eliminar por mes
	 * @return número de registros eliminados
	 */
	long deleteByMonth(AccountingCalendarDeleteMonthReq request);

	/**
	 * @brief Abre un lote de años en el calendario contable
	 * @param request solicitud con los datos para abrir el lote de años
	 * @return lista de calendarios contables creados
	 */
	List<AccountingCalendar> openYearBatch(AccountingCalendarCreateYearReq request);

	/**
	 * @brief Elimina registros por año
	 * @param request solicitud con los datos para eliminar por año
	 * @return número de registros eliminados
	 */
	long deleteByYear(AccountingCalendarDeleteYearReq request);

	/**
	 * @brief Busca todos los registros por empresa y año
	 * @param idEnterprise identificador de la empresa
	 * @param year año a buscar
	 * @return lista de calendarios contables
	 */
	List<AccountingCalendar> findAllByEnterpriseAndYear(String idEnterprise, int year);

	/**
	 * @brief Busca los años existentes por empresa
	 * @param idEnterprise identificador de la empresa
	 * @return lista de años disponibles
	 */
	List<Integer> findExistingYearsByEnterprise(String idEnterprise);

	/**
	 * @brief Verifica si existe una fecha específica para la empresa
	 * @param idEnterprise identificador de la empresa
	 * @param date fecha a verificar
	 * @return true si existe, false en caso contrario
	 */
	boolean existsDate(String idEnterprise, LocalDate date);

}


