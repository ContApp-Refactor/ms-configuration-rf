package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services.IAccountingCalendarService;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter.ConfigClientAdapter;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

/**
 * @brief Unit tests for ConfigClientAdapter
 *
 * Antes de la migración este adaptador traducía errores HTTP del cliente de
 * configuración. Ahora delega en {@link IAccountingCalendarService} dentro del
 * monolito: solo queda el contrato del puerto, la delegación y el manejo de
 * errores inesperados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigClientAdapter Tests")
class ConfigClientAdapterUnitTest {

    @Mock
    private IAccountingCalendarService accountingCalendarService;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @InjectMocks
    private ConfigClientAdapter configClientAdapter;

    private String enterpriseId;
    private LocalDate validDate;
    private LocalDate invalidDate;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        validDate = LocalDate.of(2024, 11, 15);
        invalidDate = LocalDate.of(2023, 1, 1);
    }

    @Test
    @DisplayName("Should return true when the accounting calendar says the date exists")
    void testIsValidAccountingDate_ValidDate() {
        when(accountingCalendarService.existsDate(enterpriseId, validDate)).thenReturn(true);

        boolean result = configClientAdapter.isValidAccountingDate(enterpriseId, validDate);

        assertTrue(result);
        verify(accountingCalendarService, times(1)).existsDate(enterpriseId, validDate);
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should return false when the date is outside the accounting calendar")
    void testIsValidAccountingDate_InvalidDate() {
        when(accountingCalendarService.existsDate(enterpriseId, invalidDate)).thenReturn(false);

        boolean result = configClientAdapter.isValidAccountingDate(enterpriseId, invalidDate);

        assertFalse(result);
        verify(accountingCalendarService, times(1)).existsDate(enterpriseId, invalidDate);
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should swallow a failure and report 500 when the calendar service throws")
    void testIsValidAccountingDate_ServiceThrows_ReportsGenericError() {
        when(accountingCalendarService.existsDate(enterpriseId, validDate))
            .thenThrow(new IllegalStateException("calendar unavailable"));

        boolean result = configClientAdapter.isValidAccountingDate(enterpriseId, validDate);

        assertFalse(result);
        verify(formatterResultOutputPort)
            .returnErrorGenericResponse(500, "Unexpected error communicating with configuration service");
    }

    @Test
    @DisplayName("Should propagate the error when the formatter itself refuses the request")
    void testIsValidAccountingDate_FormatterThrows_PropagatesError() {
        when(accountingCalendarService.existsDate(enterpriseId, validDate))
            .thenThrow(new IllegalStateException("calendar unavailable"));
        doThrow(new GenericErrorException(500, "Unexpected error communicating with configuration service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> configClientAdapter.isValidAccountingDate(enterpriseId, validDate));

        assertEquals("Unexpected error communicating with configuration service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should pass the enterprise and the date through untouched")
    void testIsValidAccountingDate_ParameterVerification() {
        when(accountingCalendarService.existsDate(enterpriseId, validDate)).thenReturn(true);

        configClientAdapter.isValidAccountingDate(enterpriseId, validDate);

        verify(accountingCalendarService, times(1)).existsDate(eq(enterpriseId), eq(validDate));
    }

    @Test
    @DisplayName("Should handle different enterprise IDs independently")
    void testIsValidAccountingDate_DifferentEnterpriseIds() {
        String otherEnterprise = "ENT-999";
        when(accountingCalendarService.existsDate(enterpriseId, validDate)).thenReturn(true);
        when(accountingCalendarService.existsDate(otherEnterprise, validDate)).thenReturn(false);

        assertTrue(configClientAdapter.isValidAccountingDate(enterpriseId, validDate));
        assertFalse(configClientAdapter.isValidAccountingDate(otherEnterprise, validDate));
    }

    @Test
    @DisplayName("Should handle different dates independently")
    void testIsValidAccountingDate_DifferentDates() {
        LocalDate otherDate = LocalDate.of(2024, 12, 20);
        when(accountingCalendarService.existsDate(enterpriseId, validDate)).thenReturn(true);
        when(accountingCalendarService.existsDate(enterpriseId, otherDate)).thenReturn(false);

        assertTrue(configClientAdapter.isValidAccountingDate(enterpriseId, validDate));
        assertFalse(configClientAdapter.isValidAccountingDate(enterpriseId, otherDate));
    }

    @Test
    @DisplayName("Should handle leap year dates")
    void testIsValidAccountingDate_LeapYearDate() {
        LocalDate leapYearDate = LocalDate.of(2024, 2, 29);
        when(accountingCalendarService.existsDate(enterpriseId, leapYearDate)).thenReturn(true);

        assertTrue(configClientAdapter.isValidAccountingDate(enterpriseId, leapYearDate));
    }

    @Test
    @DisplayName("Should handle a null enterprise ID without an extra error")
    void testIsValidAccountingDate_NullEnterpriseId() {
        when(accountingCalendarService.existsDate(null, validDate)).thenReturn(false);

        assertFalse(configClientAdapter.isValidAccountingDate(null, validDate));
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should handle a null date without an extra error")
    void testIsValidAccountingDate_NullDate() {
        when(accountingCalendarService.existsDate(enterpriseId, null)).thenReturn(false);

        assertFalse(configClientAdapter.isValidAccountingDate(enterpriseId, null));
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should handle an empty enterprise ID")
    void testIsValidAccountingDate_EmptyEnterpriseId() {
        when(accountingCalendarService.existsDate("", validDate)).thenReturn(false);

        assertFalse(configClientAdapter.isValidAccountingDate("", validDate));
    }

    @Test
    @DisplayName("Should support repeated calls to the same date")
    void testIsValidAccountingDate_MultipleConsecutiveCalls() {
        when(accountingCalendarService.existsDate(enterpriseId, validDate)).thenReturn(true);

        assertTrue(configClientAdapter.isValidAccountingDate(enterpriseId, validDate));
        assertTrue(configClientAdapter.isValidAccountingDate(enterpriseId, validDate));
        assertTrue(configClientAdapter.isValidAccountingDate(enterpriseId, validDate));

        verify(accountingCalendarService, times(3)).existsDate(enterpriseId, validDate);
    }
}
