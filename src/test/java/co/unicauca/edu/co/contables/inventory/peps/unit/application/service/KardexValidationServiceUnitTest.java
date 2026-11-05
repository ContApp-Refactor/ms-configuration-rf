package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import co.unicauca.edu.co.contables.inventory.peps.application.service.command.KardexDateValidationService;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IConfigClientPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KardexValidationServiceUnitTest {

    @Mock
    private IConfigClientPort configClientPort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @Mock
    private IMessageServicePort messageServicePort;

    @InjectMocks
    private KardexDateValidationService kardexDateValidationService;

    private Kardex kardex;
    private String enterpriseId;

    @BeforeEach
    void setUp() {
        kardex = new Kardex();
        enterpriseId = "ENT-001";
    }

    // ========== Tests for validateAndSetDate() - Date Assignment ==========

    @Test
    void testValidateAndSetDate_WhenDateIsNull_ShouldAddDate() {
        // Arrange
        kardex.setDate(null);
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), any(LocalDate.class));
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    void testValidateAndSetDate_WhenDateIsNull_ShouldCallAddDateMethod() {
        // Arrange
        kardex.setDate(null);
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        // After addDate() is called, kardex should have a date
        verify(configClientPort, times(1)).isValidAccountingDate(
                eq(enterpriseId), 
                argThat(date -> date != null)
        );
    }

    @Test
    void testValidateAndSetDate_WhenDateIsPresent_ShouldNotModifyDate() {
        // Arrange
        ZonedDateTime existingDate = ZonedDateTime.now().minusDays(5);
        kardex.setDate(existingDate);
        LocalDate expectedDate = existingDate.toLocalDate();
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), eq(expectedDate));
    }

    // ========== Tests for validateAndSetDate() - Valid Accounting Date ==========

    @Test
    void testValidateAndSetDate_WhenAccountingDateIsValid_ShouldNotThrowError() {
        // Arrange
        ZonedDateTime validDate = ZonedDateTime.now();
        kardex.setDate(validDate);
        when(configClientPort.isValidAccountingDate(enterpriseId, validDate.toLocalDate())).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), eq(validDate.toLocalDate()));
        verify(messageServicePort, never()).getMessage(anyString());
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    void testValidateAndSetDate_WhenAccountingDateIsValid_ShouldCallConfigClientOnce() {
        // Arrange
        ZonedDateTime validDate = ZonedDateTime.now().minusDays(10);
        kardex.setDate(validDate);
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), any(LocalDate.class));
    }

    @Test
    void testValidateAndSetDate_WithDifferentEnterpriseId_ShouldPassCorrectId() {
        // Arrange
        String differentEnterpriseId = "ENT-999";
        ZonedDateTime validDate = ZonedDateTime.now();
        kardex.setDate(validDate);
        when(configClientPort.isValidAccountingDate(differentEnterpriseId, validDate.toLocalDate())).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, differentEnterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(differentEnterpriseId), any(LocalDate.class));
    }

    // ========== Tests for validateAndSetDate() - Invalid Accounting Date ==========

    @Test
    void testValidateAndSetDate_WhenAccountingDateIsInvalid_ShouldReturnError() {
        // Arrange
        ZonedDateTime invalidDate = ZonedDateTime.now();
        kardex.setDate(invalidDate);
        String errorMessage = "Invalid accounting date";
        
        when(configClientPort.isValidAccountingDate(enterpriseId, invalidDate.toLocalDate())).thenReturn(false);
        when(messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE)).thenReturn(errorMessage);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), eq(invalidDate.toLocalDate()));
        verify(messageServicePort, times(1)).getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(400, errorMessage);
    }

    @Test
    void testValidateAndSetDate_WhenAccountingDateIsInvalid_ShouldReturn400StatusCode() {
        // Arrange
        ZonedDateTime invalidDate = ZonedDateTime.now().minusYears(2);
        kardex.setDate(invalidDate);
        String errorMessage = "Date is outside accounting period";
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(false);
        when(messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE)).thenReturn(errorMessage);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(eq(400), eq(errorMessage));
    }

    @Test
    void testValidateAndSetDate_WhenAccountingDateIsInvalid_ShouldGetMessageWithCorrectKey() {
        // Arrange
        ZonedDateTime invalidDate = ZonedDateTime.now();
        kardex.setDate(invalidDate);
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(false);
        when(messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE)).thenReturn("Error");

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(messageServicePort, times(1)).getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
    }

    // ========== Tests for validateAndSetDate() - Date Conversion ==========

    @Test
    void testValidateAndSetDate_ShouldConvertZonedDateTimeToLocalDate() {
        // Arrange
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2024-06-15T14:30:00Z");
        kardex.setDate(zonedDateTime);
        LocalDate expectedLocalDate = LocalDate.of(2024, 6, 15);
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), eq(expectedLocalDate));
    }

    @Test
    void testValidateAndSetDate_WithDateAtMidnight_ShouldPreserveDatePart() {
        // Arrange
        ZonedDateTime midnightDate = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        kardex.setDate(midnightDate);
        LocalDate expectedLocalDate = LocalDate.of(2024, 1, 1);
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), eq(expectedLocalDate));
    }

    @Test
    void testValidateAndSetDate_WithDateAtEndOfDay_ShouldPreserveDatePart() {
        // Arrange
        ZonedDateTime endOfDayDate = ZonedDateTime.parse("2024-12-31T23:59:59Z");
        kardex.setDate(endOfDayDate);
        LocalDate expectedLocalDate = LocalDate.of(2024, 12, 31);
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), eq(expectedLocalDate));
    }

    // ========== Tests for validateAndSetDate() - Complete Scenarios ==========

    @Test
    void testValidateAndSetDate_CompleteScenario_NullDateAndValid() {
        // Arrange
        kardex.setDate(null);
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), any(LocalDate.class));
        verify(messageServicePort, never()).getMessage(anyString());
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    void testValidateAndSetDate_CompleteScenario_NullDateAndInvalid() {
        // Arrange
        kardex.setDate(null);
        String errorMessage = "Accounting period is closed";
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(false);
        when(messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE)).thenReturn(errorMessage);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(enterpriseId), any(LocalDate.class));
        verify(messageServicePort, times(1)).getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(400, errorMessage);
    }

    @Test
    void testValidateAndSetDate_CompleteScenario_ExistingDateAndValid() {
        // Arrange
        ZonedDateTime existingDate = ZonedDateTime.parse("2024-07-20T10:30:00Z");
        kardex.setDate(existingDate);
        
        when(configClientPort.isValidAccountingDate(enterpriseId, existingDate.toLocalDate())).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(
                eq(enterpriseId), 
                eq(LocalDate.of(2024, 7, 20))
        );
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    void testValidateAndSetDate_CompleteScenario_ExistingDateAndInvalid() {
        // Arrange
        ZonedDateTime existingDate = ZonedDateTime.parse("2023-01-15T08:00:00Z");
        kardex.setDate(existingDate);
        String errorMessage = "Date is in a closed accounting period";
        
        when(configClientPort.isValidAccountingDate(enterpriseId, existingDate.toLocalDate())).thenReturn(false);
        when(messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE)).thenReturn(errorMessage);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(
                eq(enterpriseId), 
                eq(LocalDate.of(2023, 1, 15))
        );
        verify(messageServicePort, times(1)).getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(400, errorMessage);
    }

    // ========== Tests for validateAndSetDate() - Edge Cases ==========

    @Test
    void testValidateAndSetDate_WithLeapYearDate_ShouldValidateCorrectly() {
        // Arrange
        ZonedDateTime leapYearDate = ZonedDateTime.parse("2024-02-29T12:00:00Z");
        kardex.setDate(leapYearDate);
        
        when(configClientPort.isValidAccountingDate(enterpriseId, LocalDate.of(2024, 2, 29))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(
                eq(enterpriseId), 
                eq(LocalDate.of(2024, 2, 29))
        );
    }

    @Test
    void testValidateAndSetDate_WithFirstDayOfYear_ShouldValidateCorrectly() {
        // Arrange
        ZonedDateTime firstDayOfYear = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        kardex.setDate(firstDayOfYear);
        
        when(configClientPort.isValidAccountingDate(enterpriseId, LocalDate.of(2024, 1, 1))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(
                eq(enterpriseId), 
                eq(LocalDate.of(2024, 1, 1))
        );
    }

    @Test
    void testValidateAndSetDate_WithLastDayOfYear_ShouldValidateCorrectly() {
        // Arrange
        ZonedDateTime lastDayOfYear = ZonedDateTime.parse("2024-12-31T23:59:59Z");
        kardex.setDate(lastDayOfYear);
        
        when(configClientPort.isValidAccountingDate(enterpriseId, LocalDate.of(2024, 12, 31))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(
                eq(enterpriseId), 
                eq(LocalDate.of(2024, 12, 31))
        );
    }

    @Test
    void testValidateAndSetDate_WithEmptyEnterpriseId_ShouldStillCallValidation() {
        // Arrange
        String emptyEnterpriseId = "";
        ZonedDateTime date = ZonedDateTime.now();
        kardex.setDate(date);
        
        when(configClientPort.isValidAccountingDate(emptyEnterpriseId, date.toLocalDate())).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, emptyEnterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(emptyEnterpriseId), any(LocalDate.class));
    }

    @Test
    void testValidateAndSetDate_WithSpecialCharactersInEnterpriseId_ShouldPassThrough() {
        // Arrange
        String specialEnterpriseId = "ENT-@#$-123";
        ZonedDateTime date = ZonedDateTime.now();
        kardex.setDate(date);
        
        when(configClientPort.isValidAccountingDate(specialEnterpriseId, date.toLocalDate())).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, specialEnterpriseId);

        // Assert
        verify(configClientPort, times(1)).isValidAccountingDate(eq(specialEnterpriseId), any(LocalDate.class));
    }

    // ========== Tests for validateAndSetDate() - Interaction Verification ==========

    @Test
    void testValidateAndSetDate_ShouldCallDependenciesInCorrectOrder_ValidDate() {
        // Arrange
        ZonedDateTime date = ZonedDateTime.now();
        kardex.setDate(date);
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert - verify order
        var inOrder = inOrder(configClientPort, messageServicePort, formatterResultOutputPort);
        inOrder.verify(configClientPort).isValidAccountingDate(anyString(), any(LocalDate.class));
        inOrder.verify(messageServicePort, never()).getMessage(anyString());
        inOrder.verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    void testValidateAndSetDate_ShouldCallDependenciesInCorrectOrder_InvalidDate() {
        // Arrange
        ZonedDateTime date = ZonedDateTime.now();
        kardex.setDate(date);
        String errorMessage = "Invalid date";
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(false);
        when(messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE)).thenReturn(errorMessage);

        // Act
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);

        // Assert - verify order
        var inOrder = inOrder(configClientPort, messageServicePort, formatterResultOutputPort);
        inOrder.verify(configClientPort).isValidAccountingDate(anyString(), any(LocalDate.class));
        inOrder.verify(messageServicePort).getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
        inOrder.verify(formatterResultOutputPort).returnErrorGenericResponse(400, errorMessage);
    }

    @Test
    void testValidateAndSetDate_WithMultipleCalls_ShouldValidateEachTime() {
        // Arrange
        ZonedDateTime date1 = ZonedDateTime.now();
        ZonedDateTime date2 = ZonedDateTime.now().minusDays(5);
        
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);

        // Act
        kardex.setDate(date1);
        kardexDateValidationService.validateAndSetDate(kardex, enterpriseId);
        
        kardex.setDate(date2);
        kardexDateValidationService.validateAndSetDate(kardex, "ENT-002");

        // Assert
        verify(configClientPort, times(2)).isValidAccountingDate(anyString(), any(LocalDate.class));
    }
}
