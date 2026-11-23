package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.input.rest.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexFilterDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.validation.KardexDateRangeValidator;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KardexDateRangeValidator - Tests de validación de rangos de fechas")
class KardexDateRangeValidatorUnitTest {

    @InjectMocks
    private KardexDateRangeValidator validator;

    @Mock
    private IMessageServicePort messageService;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder violationBuilder;

    private void setupContextMocks() {
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    // ==================== Tests para valor null ====================

    @Test
    @DisplayName("isValid - Con KardexFilterDto null retorna true")
    void isValid_WithNullDto_ReturnsTrue() {
        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertTrue(result);
        verifyNoInteractions(messageService);
        verifyNoInteractions(context);
    }

    // ==================== Tests para ambas fechas null ====================

    @Test
    @DisplayName("isValid - Con ambas fechas null retorna true")
    void isValid_WithBothDatesNull_ReturnsTrue() {
        // Arrange
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(null);
        dto.setEndDate(null);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertTrue(result);
        verifyNoInteractions(messageService);
        verifyNoInteractions(context);
    }

    // ==================== Tests para una fecha null ====================

    @Test
    @DisplayName("isValid - Con startDate null y endDate presente retorna false")
    void isValid_WithStartDateNullAndEndDatePresent_ReturnsFalse() {
        // Arrange
        setupContextMocks();
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(null);
        dto.setEndDate(LocalDate.of(2025, 10, 18));

        String expectedMessage = "Both dates must be present or both must be null";
        when(messageService.getMessage("kardex.validation.date.range.incomplete", expectedMessage))
            .thenReturn(expectedMessage);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertFalse(result);
        verify(messageService, times(1)).getMessage("kardex.validation.date.range.incomplete", expectedMessage);
        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1)).buildConstraintViolationWithTemplate(expectedMessage);
    }

    @Test
    @DisplayName("isValid - Con endDate null y startDate presente retorna false")
    void isValid_WithEndDateNullAndStartDatePresent_ReturnsFalse() {
        // Arrange
        setupContextMocks();
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(LocalDate.of(2025, 10, 18));
        dto.setEndDate(null);

        String expectedMessage = "Both dates must be present or both must be null";
        when(messageService.getMessage("kardex.validation.date.range.incomplete", expectedMessage))
            .thenReturn(expectedMessage);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertFalse(result);
        verify(messageService, times(1)).getMessage("kardex.validation.date.range.incomplete", expectedMessage);
        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1)).buildConstraintViolationWithTemplate(expectedMessage);
    }

    // ==================== Tests para end antes que start ====================

    @Test
    @DisplayName("isValid - Con endDate antes de startDate retorna false")
    void isValid_WithEndDateBeforeStartDate_ReturnsFalse() {
        // Arrange
        setupContextMocks();
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(LocalDate.of(2025, 10, 18));
        dto.setEndDate(LocalDate.of(2025, 10, 10));

        String expectedMessage = "End date cannot be before start date";
        when(messageService.getMessage("kardex.validation.date.range.invalid", expectedMessage))
            .thenReturn(expectedMessage);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertFalse(result);
        verify(messageService, times(1)).getMessage("kardex.validation.date.range.invalid", expectedMessage);
        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1)).buildConstraintViolationWithTemplate(expectedMessage);
    }

    // ==================== Tests para rangos válidos ====================

    @Test
    @DisplayName("isValid - Con startDate igual a endDate retorna true")
    void isValid_WithStartDateEqualToEndDate_ReturnsTrue() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 10, 18);
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(date);
        dto.setEndDate(date);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertTrue(result);
        verifyNoInteractions(messageService);
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("isValid - Con endDate después de startDate retorna true")
    void isValid_WithEndDateAfterStartDate_ReturnsTrue() {
        // Arrange
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(LocalDate.of(2025, 10, 1));
        dto.setEndDate(LocalDate.of(2025, 10, 31));

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertTrue(result);
        verifyNoInteractions(messageService);
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("isValid - Con rango de un día retorna true")
    void isValid_WithOneDayRange_ReturnsTrue() {
        // Arrange
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(LocalDate.of(2025, 10, 18));
        dto.setEndDate(LocalDate.of(2025, 10, 19));

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertTrue(result);
        verifyNoInteractions(messageService);
    }

    @Test
    @DisplayName("isValid - Con rango de varios meses retorna true")
    void isValid_WithMultiMonthRange_ReturnsTrue() {
        // Arrange
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 12, 31));

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertTrue(result);
        verifyNoInteractions(messageService);
    }

    // ==================== Tests de mensajes i18n ====================

    @Test
    @DisplayName("isValid - Con mensaje i18n personalizado para fechas incompletas")
    void isValid_WithCustomI18nMessageForIncompleteDates() {
        // Arrange
        setupContextMocks();
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(LocalDate.of(2025, 10, 18));
        dto.setEndDate(null);

        String customMessage = "Ambas fechas deben estar presentes o ambas deben ser nulas";
        when(messageService.getMessage("kardex.validation.date.range.incomplete", 
            "Both dates must be present or both must be null"))
            .thenReturn(customMessage);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(customMessage);
    }

    @Test
    @DisplayName("isValid - Con mensaje i18n personalizado para rango inválido")
    void isValid_WithCustomI18nMessageForInvalidRange() {
        // Arrange
        setupContextMocks();
        KardexFilterDto dto = new KardexFilterDto();
        dto.setStartDate(LocalDate.of(2025, 10, 18));
        dto.setEndDate(LocalDate.of(2025, 10, 10));

        String customMessage = "La fecha de fin no puede ser anterior a la fecha de inicio";
        when(messageService.getMessage("kardex.validation.date.range.invalid", 
            "End date cannot be before start date"))
            .thenReturn(customMessage);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(customMessage);
    }
}
