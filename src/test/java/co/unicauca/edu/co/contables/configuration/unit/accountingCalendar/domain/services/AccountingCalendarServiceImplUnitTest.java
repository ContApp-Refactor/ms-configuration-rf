package co.unicauca.edu.co.contables.configuration.unit.accountingCalendar.domain.services;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.mapper.AccountingCalendarDataMapper;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.repository.AccountingCalendarRepository;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.mapper.AccountingCalendarDomainMapper;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services.AccountingCalendarServiceImpl;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request.*;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar.AccountingCalendarDateExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar.AccountingCalendarInvalidDateException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.accountingCalendar.AccountingCalendarNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountingCalendarServiceImplUnitTest {

    @Mock
    private AccountingCalendarRepository repository;

    @Mock
    private AccountingCalendarDataMapper dataMapper;

    @Mock
    private AccountingCalendarDomainMapper domainMapper;

    @InjectMocks
    private AccountingCalendarServiceImpl service;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 6, 15);
    private static final String TEST_DATE_STRING = "2024-06-15";

    private AccountingCalendar domain;
    private AccountingCalendarEntity entity;
    private AccountingCalendarEntity savedEntity;

    @BeforeEach
    void setUp() {
        domain = AccountingCalendar.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .date(TEST_DATE)
                .build();

        entity = AccountingCalendarEntity.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .date(TEST_DATE)
                .build();

        savedEntity = AccountingCalendarEntity.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .date(TEST_DATE)
                .build();
    }

    @Test
    @DisplayName("Debe crear un calendario contable exitosamente")
    void testCreateSuccess() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate(TEST_DATE_STRING);
        AccountingCalendar domainWithoutId = AccountingCalendar.builder()
                .idEnterprise(ENTERPRISE_ID)
                .build();
        when(repository.existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE)).thenReturn(false);
        when(domainMapper.toDomain(request)).thenReturn(domainWithoutId);
        when(dataMapper.toEntity(any(AccountingCalendar.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(dataMapper.toDomain(savedEntity)).thenReturn(domain);

        AccountingCalendar result = service.create(request);

        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(ENTERPRISE_ID, result.getIdEnterprise());
        verify(repository).existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la fecha ya existe")
    void testCreateThrowsExceptionWhenDateExists() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate(TEST_DATE_STRING);
        when(repository.existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE)).thenReturn(true);

        assertThrows(AccountingCalendarDateExistsException.class,
                () -> service.create(request));

        verify(repository).existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la fecha es null")
    void testCreateThrowsExceptionWhenDateIsNull() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate(null);

        assertThrows(AccountingCalendarInvalidDateException.class,
                () -> service.create(request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la fecha está vacía")
    void testCreateThrowsExceptionWhenDateIsEmpty() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("   ");

        assertThrows(AccountingCalendarInvalidDateException.class,
                () -> service.create(request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el formato de fecha es inválido")
    void testCreateThrowsExceptionWhenDateFormatIsInvalid() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("15-06-2024");

        assertThrows(AccountingCalendarInvalidDateException.class,
                () -> service.create(request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el año es menor a 2000")
    void testCreateThrowsExceptionWhenYearLessThan2000() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("1999-06-15");

        assertThrows(AccountingCalendarInvalidDateException.class,
                () -> service.create(request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe aceptar fecha con año límite inferior 2000")
    void testCreateAcceptsYearBoundary2000() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("2000-01-01");
        LocalDate expectedDate = LocalDate.of(2000, 1, 1);
        AccountingCalendar domainWithoutId = AccountingCalendar.builder()
                .idEnterprise(ENTERPRISE_ID)
                .build();
        AccountingCalendarEntity entityForDate = AccountingCalendarEntity.builder()
                .idEnterprise(ENTERPRISE_ID)
                .date(expectedDate)
                .build();
        when(repository.existsByIdEnterpriseAndDate(ENTERPRISE_ID, expectedDate)).thenReturn(false);
        when(domainMapper.toDomain(request)).thenReturn(domainWithoutId);
        when(dataMapper.toEntity(any(AccountingCalendar.class))).thenReturn(entityForDate);
        when(repository.save(entityForDate)).thenReturn(entityForDate);
        when(dataMapper.toDomain(entityForDate)).thenReturn(domain);

        AccountingCalendar result = service.create(request);

        assertNotNull(result);
        verify(repository).existsByIdEnterpriseAndDate(ENTERPRISE_ID, expectedDate);
    }

    @Test
    @DisplayName("Debe retornar calendario contable cuando existe")
    void testFindByIdSuccess() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        AccountingCalendar result = service.findById(ID, ENTERPRISE_ID);

        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(ENTERPRISE_ID, result.getIdEnterprise());
        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no existe")
    void testFindByIdThrowsExceptionWhenNotFound() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThrows(AccountingCalendarNotFoundException.class,
                () -> service.findById(ID, ENTERPRISE_ID));

        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe eliminar calendario contable cuando existe")
    void testDeleteSuccess() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));

        service.delete(ID, ENTERPRISE_ID);

        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no existe al eliminar")
    void testDeleteThrowsExceptionWhenNotFound() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThrows(AccountingCalendarNotFoundException.class,
                () -> service.delete(ID, ENTERPRISE_ID));

        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe crear todas las fechas del mes cuando no existe ninguna")
    void testOpenMonthBatchCreatesAllDatesWhenNoneExist() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        request.setMonth(2);
        LocalDate startOfMonth = LocalDate.of(2024, 2, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 2, 29);
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        List<AccountingCalendar> result = service.openMonthBatch(request);

        assertNotNull(result);
        assertEquals(29, result.size());
        verify(repository).findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth);
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe crear solo fechas faltantes cuando algunas ya existen")
    void testOpenMonthBatchCreatesOnlyMissingDates() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        request.setMonth(2);
        LocalDate startOfMonth = LocalDate.of(2024, 2, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 2, 29);
        List<AccountingCalendarEntity> existingEntities = List.of(
                AccountingCalendarEntity.builder().idEnterprise(ENTERPRISE_ID).date(LocalDate.of(2024, 2, 1)).build(),
                AccountingCalendarEntity.builder().idEnterprise(ENTERPRISE_ID).date(LocalDate.of(2024, 2, 15)).build());
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(existingEntities);
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        List<AccountingCalendar> result = service.openMonthBatch(request);

        assertEquals(27, result.size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountingCalendarEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        List<AccountingCalendarEntity> savedEntities = captor.getValue();
        assertEquals(27, savedEntities.size());
        assertTrue(savedEntities.stream().noneMatch(e -> e.getDate().equals(LocalDate.of(2024, 2, 1))));
        assertTrue(savedEntities.stream().noneMatch(e -> e.getDate().equals(LocalDate.of(2024, 2, 15))));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando todas las fechas ya existen")
    void testOpenMonthBatchReturnsEmptyWhenAllDatesExist() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        request.setMonth(4);
        LocalDate startOfMonth = LocalDate.of(2024, 4, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 4, 30);
        List<AccountingCalendarEntity> existingEntities = startOfMonth.datesUntil(endOfMonth.plusDays(1))
                .map(date -> AccountingCalendarEntity.builder().idEnterprise(ENTERPRISE_ID).date(date).build())
                .toList();
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(existingEntities);
        when(repository.saveAll(anyList())).thenReturn(Collections.emptyList());

        List<AccountingCalendar> result = service.openMonthBatch(request);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe manejar correctamente mes con 31 días")
    void testOpenMonthBatchHandles31DaysMonth() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        request.setMonth(1);
        LocalDate startOfMonth = LocalDate.of(2024, 1, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 1, 31);
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        List<AccountingCalendar> result = service.openMonthBatch(request);

        assertEquals(31, result.size());
    }

    @Test
    @DisplayName("Debe manejar correctamente febrero en año no bisiesto")
    void testOpenMonthBatchHandlesFebruaryNonLeapYear() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2023);
        request.setMonth(2);
        LocalDate startOfMonth = LocalDate.of(2023, 2, 1);
        LocalDate endOfMonth = LocalDate.of(2023, 2, 28);
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        List<AccountingCalendar> result = service.openMonthBatch(request);

        assertEquals(28, result.size());
    }

    @Test
    @DisplayName("Debe eliminar todas las fechas del mes")
    void testDeleteByMonthSuccess() {
        AccountingCalendarDeleteMonthReq request = new AccountingCalendarDeleteMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        request.setMonth(6);
        LocalDate startOfMonth = LocalDate.of(2024, 6, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 6, 30);
        when(repository.deleteByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(30L);

        long deletedCount = service.deleteByMonth(request);

        assertEquals(30L, deletedCount);
        verify(repository).deleteByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no hay fechas para eliminar")
    void testDeleteByMonthReturnsZeroWhenNoDates() {
        AccountingCalendarDeleteMonthReq request = new AccountingCalendarDeleteMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        request.setMonth(6);
        LocalDate startOfMonth = LocalDate.of(2024, 6, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 6, 30);
        when(repository.deleteByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(0L);

        long deletedCount = service.deleteByMonth(request);

        assertEquals(0L, deletedCount);
    }

    @Test
    @DisplayName("Debe crear todas las fechas del año cuando no existe ninguna")
    void testOpenYearBatchCreatesAllDatesWhenNoneExist() {
        AccountingCalendarCreateYearReq request = new AccountingCalendarCreateYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        LocalDate startOfYear = LocalDate.of(2024, 1, 1);
        LocalDate endOfYear = LocalDate.of(2024, 12, 31);
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        List<AccountingCalendar> result = service.openYearBatch(request);

        assertEquals(366, result.size());
        verify(repository).findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear);
    }

    @Test
    @DisplayName("Debe crear 365 días para año no bisiesto")
    void testOpenYearBatchCreates365DaysForNonLeapYear() {
        AccountingCalendarCreateYearReq request = new AccountingCalendarCreateYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2023);
        LocalDate startOfYear = LocalDate.of(2023, 1, 1);
        LocalDate endOfYear = LocalDate.of(2023, 12, 31);
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        List<AccountingCalendar> result = service.openYearBatch(request);

        assertEquals(365, result.size());
    }

    @Test
    @DisplayName("Debe crear solo fechas faltantes cuando algunas ya existen")
    void testOpenYearBatchCreatesOnlyMissingDates() {
        AccountingCalendarCreateYearReq request = new AccountingCalendarCreateYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        LocalDate startOfYear = LocalDate.of(2024, 1, 1);
        LocalDate endOfYear = LocalDate.of(2024, 12, 31);
        List<AccountingCalendarEntity> existingEntities = List.of(
                AccountingCalendarEntity.builder().idEnterprise(ENTERPRISE_ID).date(LocalDate.of(2024, 1, 1)).build(),
                AccountingCalendarEntity.builder().idEnterprise(ENTERPRISE_ID).date(LocalDate.of(2024, 6, 15)).build(),
                AccountingCalendarEntity.builder().idEnterprise(ENTERPRISE_ID).date(LocalDate.of(2024, 12, 31))
                        .build());
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(existingEntities);
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        List<AccountingCalendar> result = service.openYearBatch(request);

        assertEquals(363, result.size());
    }

    @Test
    @DisplayName("Debe eliminar todas las fechas del año")
    void testDeleteByYearSuccess() {
        AccountingCalendarDeleteYearReq request = new AccountingCalendarDeleteYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        LocalDate startOfYear = LocalDate.of(2024, 1, 1);
        LocalDate endOfYear = LocalDate.of(2024, 12, 31);
        when(repository.deleteByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(366L);

        long deletedCount = service.deleteByYear(request);

        assertEquals(366L, deletedCount);
        verify(repository).deleteByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no hay fechas para eliminar")
    void testDeleteByYearReturnsZeroWhenNoDates() {
        AccountingCalendarDeleteYearReq request = new AccountingCalendarDeleteYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        LocalDate startOfYear = LocalDate.of(2024, 1, 1);
        LocalDate endOfYear = LocalDate.of(2024, 12, 31);
        when(repository.deleteByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(0L);

        long deletedCount = service.deleteByYear(request);

        assertEquals(0L, deletedCount);
    }

    @Test
    @DisplayName("Debe retornar todas las fechas del año para la empresa")
    void testFindAllByEnterpriseAndYearSuccess() {
        int year = 2024;
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        List<AccountingCalendarEntity> entities = List.of(entity);
        when(repository.findAllByIdEnterpriseAndDateBetweenOrderByDateAsc(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(entities);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        List<AccountingCalendar> result = service.findAllByEnterpriseAndYear(ENTERPRISE_ID, year);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).findAllByIdEnterpriseAndDateBetweenOrderByDateAsc(ENTERPRISE_ID, startOfYear, endOfYear);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay fechas")
    void testFindAllByEnterpriseAndYearReturnsEmptyList() {
        int year = 2024;
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        when(repository.findAllByIdEnterpriseAndDateBetweenOrderByDateAsc(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(Collections.emptyList());

        List<AccountingCalendar> result = service.findAllByEnterpriseAndYear(ENTERPRISE_ID, year);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar lista de años existentes")
    void testFindExistingYearsByEnterpriseSuccess() {
        List<Integer> years = List.of(2022, 2023, 2024);
        when(repository.findDistinctYearsByIdEnterprise(ENTERPRISE_ID)).thenReturn(years);

        List<Integer> result = service.findExistingYearsByEnterprise(ENTERPRISE_ID);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(years));
        verify(repository).findDistinctYearsByIdEnterprise(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay años")
    void testFindExistingYearsByEnterpriseReturnsEmptyList() {
        when(repository.findDistinctYearsByIdEnterprise(ENTERPRISE_ID)).thenReturn(Collections.emptyList());

        List<Integer> result = service.findExistingYearsByEnterprise(ENTERPRISE_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar true cuando la fecha existe")
    void testExistsDateReturnsTrue() {
        when(repository.existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE)).thenReturn(true);

        boolean result = service.existsDate(ENTERPRISE_ID, TEST_DATE);

        assertTrue(result);
        verify(repository).existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE);
    }

    @Test
    @DisplayName("Debe retornar false cuando la fecha no existe")
    void testExistsDateReturnsFalse() {
        when(repository.existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE)).thenReturn(false);

        boolean result = service.existsDate(ENTERPRISE_ID, TEST_DATE);

        assertFalse(result);
        verify(repository).existsByIdEnterpriseAndDate(ENTERPRISE_ID, TEST_DATE);
    }

    @Test
    @DisplayName("Debe rechazar fecha con texto inválido")
    void testCreateRejectsInvalidDateText() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("fecha-invalida");

        AccountingCalendarInvalidDateException exception = assertThrows(
                AccountingCalendarInvalidDateException.class,
                () -> service.create(request));

        assertTrue(exception.getMessage().contains("Formato de fecha inválido"));
    }

    @Test
    @DisplayName("Debe rechazar fecha con día inválido")
    void testCreateRejectsInvalidDay() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("2024-02-30");

        assertThrows(AccountingCalendarInvalidDateException.class,
                () -> service.create(request));
    }

    @Test
    @DisplayName("Debe rechazar fecha con mes inválido")
    void testCreateRejectsInvalidMonth() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("2024-13-01");

        assertThrows(AccountingCalendarInvalidDateException.class,
                () -> service.create(request));
    }

    @Test
    @DisplayName("Debe aceptar fecha válida en límite superior de año")
    void testCreateAcceptsValidHighYearBoundary() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("9999-12-31");
        LocalDate expectedDate = LocalDate.of(9999, 12, 31);
        AccountingCalendar domainWithoutId = AccountingCalendar.builder()
                .idEnterprise(ENTERPRISE_ID)
                .build();
        AccountingCalendarEntity entityForDate = AccountingCalendarEntity.builder()
                .idEnterprise(ENTERPRISE_ID)
                .date(expectedDate)
                .build();
        when(repository.existsByIdEnterpriseAndDate(ENTERPRISE_ID, expectedDate)).thenReturn(false);
        when(domainMapper.toDomain(request)).thenReturn(domainWithoutId);
        when(dataMapper.toEntity(any(AccountingCalendar.class))).thenReturn(entityForDate);
        when(repository.save(entityForDate)).thenReturn(entityForDate);
        when(dataMapper.toDomain(entityForDate)).thenReturn(domain);

        AccountingCalendar result = service.create(request);

        assertNotNull(result);
        verify(repository).existsByIdEnterpriseAndDate(ENTERPRISE_ID, expectedDate);
    }

    @Test
    @DisplayName("Debe crear entidades con idEnterprise y fecha correctos en openMonthBatch")
    void testOpenMonthBatchCreatesEntitiesWithCorrectData() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        request.setMonth(4);
        LocalDate startOfMonth = LocalDate.of(2024, 4, 1);
        LocalDate endOfMonth = LocalDate.of(2024, 4, 30);
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfMonth, endOfMonth))
                .thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        service.openMonthBatch(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountingCalendarEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        List<AccountingCalendarEntity> savedEntities = captor.getValue();

        assertTrue(savedEntities.stream().allMatch(e -> ENTERPRISE_ID.equals(e.getIdEnterprise())));
        assertTrue(savedEntities.stream().allMatch(e -> e.getDate().getYear() == 2024));
        assertTrue(savedEntities.stream().allMatch(e -> e.getDate().getMonthValue() == 4));
        assertEquals(LocalDate.of(2024, 4, 1), savedEntities.get(0).getDate());
        assertEquals(LocalDate.of(2024, 4, 30), savedEntities.get(savedEntities.size() - 1).getDate());
    }

    @Test
    @DisplayName("Debe crear entidades con idEnterprise y fecha correctos en openYearBatch")
    void testOpenYearBatchCreatesEntitiesWithCorrectData() {
        AccountingCalendarCreateYearReq request = new AccountingCalendarCreateYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(2024);
        LocalDate startOfYear = LocalDate.of(2024, 1, 1);
        LocalDate endOfYear = LocalDate.of(2024, 12, 31);
        when(repository.findAllByIdEnterpriseAndDateBetween(ENTERPRISE_ID, startOfYear, endOfYear))
                .thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dataMapper.toDomain(any(AccountingCalendarEntity.class))).thenReturn(domain);

        service.openYearBatch(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountingCalendarEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        List<AccountingCalendarEntity> savedEntities = captor.getValue();

        assertTrue(savedEntities.stream().allMatch(e -> ENTERPRISE_ID.equals(e.getIdEnterprise())));
        assertTrue(savedEntities.stream().allMatch(e -> e.getDate().getYear() == 2024));
        assertEquals(LocalDate.of(2024, 1, 1), savedEntities.get(0).getDate());
        assertEquals(LocalDate.of(2024, 12, 31), savedEntities.get(savedEntities.size() - 1).getDate());
    }

}
