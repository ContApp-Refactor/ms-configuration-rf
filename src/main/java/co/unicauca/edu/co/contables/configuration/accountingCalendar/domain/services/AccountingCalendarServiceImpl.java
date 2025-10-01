package co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.mapper.AccountingCalendarDataMapper;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.repository.AccountingCalendarRepository;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.mapper.AccountingCalendarDomainMapper;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request.*;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar.AccountingCalendarDateExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar.AccountingCalendarNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar.AccountingCalendarInvalidDateException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountingCalendarServiceImpl implements IAccountingCalendarService {

        private final AccountingCalendarRepository repository;
        private final AccountingCalendarDataMapper dataMapper;
        private final AccountingCalendarDomainMapper domainMapper;

        @Override
        @Transactional
        public AccountingCalendar create(AccountingCalendarCreateReq request) {
                // Parsear y validar la fecha
                LocalDate date = parseAndValidateDate(request.getDate());

                // Validación de negocio: verificar que no exista duplicado
                boolean exists = repository.existsByIdEnterpriseAndDate(
                                request.getIdEnterprise(), date);
                if (exists) {
                        throw new AccountingCalendarDateExistsException();
                }

                // Crear y guardar
                AccountingCalendar domain = domainMapper.toDomain(request);
                domain.setDate(date); // Asignar la fecha parseada
                AccountingCalendarEntity saved = repository.save(dataMapper.toEntity(domain));
                return dataMapper.toDomain(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public AccountingCalendar findById(Long id, String idEnterprise) {
                return dataMapper.toDomain(repository.findByIdAndIdEnterprise(id, idEnterprise)
                                .orElseThrow(AccountingCalendarNotFoundException::new));
        }

        @Override
        @Transactional
        public void delete(Long id, String idEnterprise) {
                AccountingCalendarEntity entity = repository.findByIdAndIdEnterprise(id, idEnterprise)
                                .orElseThrow(AccountingCalendarNotFoundException::new);
                repository.delete(entity);
        }

        @Override
        @Transactional
        public List<AccountingCalendar> openMonthBatch(AccountingCalendarCreateMonthReq request) {
                // Las validaciones básicas (rango de año y mes) ya están en @Min/@Max del DTO
                YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
                LocalDate start = ym.atDay(1);
                LocalDate end = ym.atEndOfMonth();

                // 1. Generar todas las fechas del mes
                List<LocalDate> datesToCreate = generateDateRange(start, end);

                // 2. Buscar fechas que ya existen
                List<AccountingCalendarEntity> existingEntities = repository.findAllByIdEnterpriseAndDateBetween(
                                request.getIdEnterprise(), start, end);
                // 3. Usar HashSet para búsqueda
                Set<LocalDate> existingDates = existingEntities.stream()
                                .map(AccountingCalendarEntity::getDate)
                                .collect(Collectors.toSet());

                // 3. Filtrar fechas que no existen
                List<LocalDate> newDates = datesToCreate.stream()
                                .filter(date -> !existingDates.contains(date))
                                .collect(Collectors.toList());

                // 4. Crear entidades en batch
                List<AccountingCalendarEntity> entities = newDates.stream()
                                .map(date -> createEntity(request.getIdEnterprise(), date))
                                .collect(Collectors.toList());

                // 5. Insertar en batch
                List<AccountingCalendarEntity> saved = repository.saveAll(entities);

                return saved.stream()
                                .map(dataMapper::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public long deleteByMonth(AccountingCalendarDeleteMonthReq request) {
                YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
                LocalDate start = ym.atDay(1);
                LocalDate end = ym.atEndOfMonth();
                return repository.deleteByIdEnterpriseAndDateBetween(
                                request.getIdEnterprise(), start, end);
        }

        @Override
        @Transactional
        public List<AccountingCalendar> openYearBatch(AccountingCalendarCreateYearReq request) {
                // Las validaciones básicas (rango de año) ya están en @Min/@Max del DTO
                LocalDate start = LocalDate.of(request.getYear(), 1, 1);
                LocalDate end = LocalDate.of(request.getYear(), 12, 31);

                // 1. Generar todas las fechas del año
                List<LocalDate> datesToCreate = generateDateRange(start, end);

                // 2. Buscar fechas que ya existen
                List<AccountingCalendarEntity> existingEntities = repository.findAllByIdEnterpriseAndDateBetween(
                                request.getIdEnterprise(), start, end);
                // 3. Usar HashSet para búsqueda
                Set<LocalDate> existingDates = existingEntities.stream()
                                .map(AccountingCalendarEntity::getDate)
                                .collect(Collectors.toSet());

                // 3. Filtrar fechas que no existen
                List<LocalDate> newDates = datesToCreate.stream()
                                .filter(date -> !existingDates.contains(date))
                                .collect(Collectors.toList());

                // 4. Crear entidades en batch
                List<AccountingCalendarEntity> entities = newDates.stream()
                                .map(date -> createEntity(request.getIdEnterprise(), date))
                                .collect(Collectors.toList());

                // 5. Insertar en batch
                List<AccountingCalendarEntity> saved = repository.saveAll(entities);

                return saved.stream()
                                .map(dataMapper::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public long deleteByYear(AccountingCalendarDeleteYearReq request) {
                LocalDate start = LocalDate.of(request.getYear(), 1, 1);
                LocalDate end = LocalDate.of(request.getYear(), 12, 31);
                return repository.deleteByIdEnterpriseAndDateBetween(
                                request.getIdEnterprise(), start, end);
        }

        @Override
        @Transactional(readOnly = true)
        public List<AccountingCalendar> findAllByEnterpriseAndYear(String idEnterprise, int year) {
                LocalDate startOfYear = LocalDate.of(year, 1, 1);
                LocalDate endOfYear = LocalDate.of(year, 12, 31);
                List<AccountingCalendarEntity> entities = repository.findAllByIdEnterpriseAndDateBetweenOrderByDateAsc(
                                idEnterprise, startOfYear, endOfYear);
                return entities.stream()
                                .map(dataMapper::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<Integer> findExistingYearsByEnterprise(String idEnterprise) {
                return repository.findDistinctYearsByIdEnterprise(idEnterprise);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean existsDate(String idEnterprise, LocalDate date) {
                return repository.existsByIdEnterpriseAndDate(idEnterprise, date);
        }

        // Métodos auxiliares privados
        private LocalDate parseAndValidateDate(String dateString) {
                // Validar que no sea null o vacío
                if (dateString == null || dateString.trim().isEmpty()) {
                        throw new AccountingCalendarInvalidDateException("La fecha es obligatoria");
                }

                LocalDate date;
                try {
                        // Intentar parsear la fecha
                        date = LocalDate.parse(dateString);
                } catch (Exception e) {
                        throw new AccountingCalendarInvalidDateException(
                                        "Formato de fecha inválido: " + dateString + ". Use el formato YYYY-MM-DD", e);
                }

                // Validar rango de año
                int year = date.getYear();
                if (year < 2000 || year > 9999) {
                        throw new AccountingCalendarInvalidDateException(
                                        "El año debe estar entre 2000 y 9999. Año recibido: " + year);
                }

                return date;
        }

        private List<LocalDate> generateDateRange(LocalDate start, LocalDate end) {
                return start.datesUntil(end.plusDays(1))
                                .collect(Collectors.toList());
        }

        private AccountingCalendarEntity createEntity(String idEnterprise, LocalDate date) {
                return AccountingCalendarEntity.builder()
                                .idEnterprise(idEnterprise)
                                .date(date)
                                .build();
        }

}
