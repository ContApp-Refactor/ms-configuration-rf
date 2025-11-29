package co.unicauca.edu.co.contables.configuration.unit.accountingCalendar.presentation.controller;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.mapper.AccountingCalendarDomainMapper;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services.IAccountingCalendarService;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request.*;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.response.AccountingCalendarRes;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.controller.AccountingCalendarController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountingCalendarControllerUnitTest {

    @Mock
    private IAccountingCalendarService service;

    @Mock
    private AccountingCalendarDomainMapper mapper;

    @InjectMocks
    private AccountingCalendarController controller;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 6, 15);
    private static final int TEST_YEAR = 2024;
    private static final int TEST_MONTH = 6;

    private AccountingCalendar domain;
    private AccountingCalendarRes response;

    @BeforeEach
    void setUp() {
        domain = AccountingCalendar.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .date(TEST_DATE)
                .build();

        response = new AccountingCalendarRes();
        response.setId(ID);
        response.setIdEnterprise(ENTERPRISE_ID);
        response.setDate(TEST_DATE);
    }

    @Test
    @DisplayName("create - Debe crear calendario contable y retornar 200 OK")
    void testCreateSuccess() {
        AccountingCalendarCreateReq request = new AccountingCalendarCreateReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setDate("2024-06-15");
        when(service.create(request)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        ResponseEntity<AccountingCalendarRes> result = controller.create(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        assertEquals(ENTERPRISE_ID, result.getBody().getIdEnterprise());
        verify(service).create(request);
        verify(mapper).toRes(domain);
    }

    @Test
    @DisplayName("getById - Debe retornar calendario contable cuando existe")
    void testGetByIdSuccess() {
        when(service.findById(ID, ENTERPRISE_ID)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        ResponseEntity<AccountingCalendarRes> result = controller.getById(ID, ENTERPRISE_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        verify(service).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("delete - Debe eliminar calendario contable y retornar 204 No Content")
    void testDeleteSuccess() {
        doNothing().when(service).delete(ID, ENTERPRISE_ID);

        ResponseEntity<Void> result = controller.delete(ID, ENTERPRISE_ID);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(service).delete(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("openMonthBatch - Debe crear fechas del mes y retornar 200 OK")
    void testOpenMonthBatchSuccess() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        request.setMonth(TEST_MONTH);
        List<AccountingCalendar> domains = List.of(domain);
        when(service.openMonthBatch(request)).thenReturn(domains);
        when(mapper.toRes(domain)).thenReturn(response);

        ResponseEntity<List<AccountingCalendarRes>> result = controller.openMonthBatch(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(service).openMonthBatch(request);
    }

    @Test
    @DisplayName("openMonthBatch - Debe retornar lista vacía cuando no se crean fechas")
    void testOpenMonthBatchReturnsEmptyList() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        request.setMonth(TEST_MONTH);
        when(service.openMonthBatch(request)).thenReturn(Collections.emptyList());

        ResponseEntity<List<AccountingCalendarRes>> result = controller.openMonthBatch(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("deleteByMonth - Debe eliminar fechas del mes y retornar 204 No Content")
    void testDeleteByMonthSuccess() {
        AccountingCalendarDeleteMonthReq request = new AccountingCalendarDeleteMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        request.setMonth(TEST_MONTH);
        when(service.deleteByMonth(request)).thenReturn(30L);

        ResponseEntity<Void> result = controller.deleteByMonth(request);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(service).deleteByMonth(request);
    }

    @Test
    @DisplayName("openYearBatch - Debe crear fechas del año y retornar 200 OK")
    void testOpenYearBatchSuccess() {
        AccountingCalendarCreateYearReq request = new AccountingCalendarCreateYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        List<AccountingCalendar> domains = List.of(domain);
        when(service.openYearBatch(request)).thenReturn(domains);
        when(mapper.toRes(domain)).thenReturn(response);

        ResponseEntity<List<AccountingCalendarRes>> result = controller.openYearBatch(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(service).openYearBatch(request);
    }

    @Test
    @DisplayName("openYearBatch - Debe retornar lista vacía cuando no se crean fechas")
    void testOpenYearBatchReturnsEmptyList() {
        AccountingCalendarCreateYearReq request = new AccountingCalendarCreateYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        when(service.openYearBatch(request)).thenReturn(Collections.emptyList());

        ResponseEntity<List<AccountingCalendarRes>> result = controller.openYearBatch(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("deleteByYear - Debe eliminar fechas del año y retornar 204 No Content")
    void testDeleteByYearSuccess() {
        AccountingCalendarDeleteYearReq request = new AccountingCalendarDeleteYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        when(service.deleteByYear(request)).thenReturn(366L);

        ResponseEntity<Void> result = controller.deleteByYear(request);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(service).deleteByYear(request);
    }

    @Test
    @DisplayName("findAllByYear - Debe retornar lista de calendarios del año")
    void testFindAllByYearSuccess() {
        List<AccountingCalendar> domains = List.of(domain);
        when(service.findAllByEnterpriseAndYear(ENTERPRISE_ID, TEST_YEAR)).thenReturn(domains);
        when(mapper.toRes(domain)).thenReturn(response);

        ResponseEntity<List<AccountingCalendarRes>> result = controller.findAllByYear(ENTERPRISE_ID, TEST_YEAR);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(service).findAllByEnterpriseAndYear(ENTERPRISE_ID, TEST_YEAR);
    }

    @Test
    @DisplayName("findAllByYear - Debe retornar lista vacía cuando no hay calendarios")
    void testFindAllByYearReturnsEmptyList() {
        when(service.findAllByEnterpriseAndYear(ENTERPRISE_ID, TEST_YEAR)).thenReturn(Collections.emptyList());

        ResponseEntity<List<AccountingCalendarRes>> result = controller.findAllByYear(ENTERPRISE_ID, TEST_YEAR);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("findExistingYears - Debe retornar lista de años existentes")
    void testFindExistingYearsSuccess() {
        List<Integer> years = List.of(2022, 2023, 2024);
        when(service.findExistingYearsByEnterprise(ENTERPRISE_ID)).thenReturn(years);

        ResponseEntity<List<Integer>> result = controller.findExistingYears(ENTERPRISE_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(3, result.getBody().size());
        assertTrue(result.getBody().containsAll(years));
        verify(service).findExistingYearsByEnterprise(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("findExistingYears - Debe retornar lista vacía cuando no hay años")
    void testFindExistingYearsReturnsEmptyList() {
        when(service.findExistingYearsByEnterprise(ENTERPRISE_ID)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Integer>> result = controller.findExistingYears(ENTERPRISE_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("existsDate - Debe retornar true cuando la fecha existe")
    void testExistsDateReturnsTrue() {
        when(service.existsDate(ENTERPRISE_ID, TEST_DATE)).thenReturn(true);

        ResponseEntity<Boolean> result = controller.existsDate(ENTERPRISE_ID, TEST_DATE);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody());
        verify(service).existsDate(ENTERPRISE_ID, TEST_DATE);
    }

    @Test
    @DisplayName("existsDate - Debe retornar false cuando la fecha no existe")
    void testExistsDateReturnsFalse() {
        when(service.existsDate(ENTERPRISE_ID, TEST_DATE)).thenReturn(false);

        ResponseEntity<Boolean> result = controller.existsDate(ENTERPRISE_ID, TEST_DATE);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody());
    }

    @Test
    @DisplayName("openMonthBatch - Debe mapear múltiples dominios a respuestas")
    void testOpenMonthBatchMapsMultipleDomains() {
        AccountingCalendarCreateMonthReq request = new AccountingCalendarCreateMonthReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        request.setMonth(TEST_MONTH);
        AccountingCalendar domain2 = AccountingCalendar.builder()
                .id(2L)
                .idEnterprise(ENTERPRISE_ID)
                .date(TEST_DATE.plusDays(1))
                .build();
        AccountingCalendarRes response2 = new AccountingCalendarRes();
        response2.setId(2L);
        response2.setIdEnterprise(ENTERPRISE_ID);
        response2.setDate(TEST_DATE.plusDays(1));
        List<AccountingCalendar> domains = List.of(domain, domain2);
        when(service.openMonthBatch(request)).thenReturn(domains);
        when(mapper.toRes(domain)).thenReturn(response);
        when(mapper.toRes(domain2)).thenReturn(response2);

        ResponseEntity<List<AccountingCalendarRes>> result = controller.openMonthBatch(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        verify(mapper, times(2)).toRes(any(AccountingCalendar.class));
    }

    @Test
    @DisplayName("openYearBatch - Debe mapear múltiples dominios a respuestas")
    void testOpenYearBatchMapsMultipleDomains() {
        AccountingCalendarCreateYearReq request = new AccountingCalendarCreateYearReq();
        request.setIdEnterprise(ENTERPRISE_ID);
        request.setYear(TEST_YEAR);
        AccountingCalendar domain2 = AccountingCalendar.builder()
                .id(2L)
                .idEnterprise(ENTERPRISE_ID)
                .date(TEST_DATE.plusDays(1))
                .build();
        AccountingCalendarRes response2 = new AccountingCalendarRes();
        response2.setId(2L);
        List<AccountingCalendar> domains = List.of(domain, domain2);
        when(service.openYearBatch(request)).thenReturn(domains);
        when(mapper.toRes(domain)).thenReturn(response);
        when(mapper.toRes(domain2)).thenReturn(response2);

        ResponseEntity<List<AccountingCalendarRes>> result = controller.openYearBatch(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        verify(mapper, times(2)).toRes(any(AccountingCalendar.class));
    }

    @Test
    @DisplayName("findAllByYear - Debe mapear múltiples dominios a respuestas")
    void testFindAllByYearMapsMultipleDomains() {
        AccountingCalendar domain2 = AccountingCalendar.builder()
                .id(2L)
                .idEnterprise(ENTERPRISE_ID)
                .date(TEST_DATE.plusDays(1))
                .build();
        AccountingCalendarRes response2 = new AccountingCalendarRes();
        response2.setId(2L);
        List<AccountingCalendar> domains = List.of(domain, domain2);
        when(service.findAllByEnterpriseAndYear(ENTERPRISE_ID, TEST_YEAR)).thenReturn(domains);
        when(mapper.toRes(domain)).thenReturn(response);
        when(mapper.toRes(domain2)).thenReturn(response2);

        ResponseEntity<List<AccountingCalendarRes>> result = controller.findAllByYear(ENTERPRISE_ID, TEST_YEAR);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        verify(mapper, times(2)).toRes(any(AccountingCalendar.class));
    }
}
