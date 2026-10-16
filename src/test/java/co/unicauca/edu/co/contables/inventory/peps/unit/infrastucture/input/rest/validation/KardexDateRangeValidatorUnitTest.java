package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.input.rest.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexByDateDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.validation.KardexDateRangeValidator;

/**
 * @brief Unit tests for KardexDateRangeValidator
 * 
 * Tests the validation logic for date range consistency in kardex requests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexDateRangeValidator Tests")
public class KardexDateRangeValidatorUnitTest {
    
    @Mock
    private IMessageServicePort messageService;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @Mock
    private ConstraintViolationBuilder violationBuilder;
    
    @InjectMocks
    private KardexDateRangeValidator validator;
    
    private KardexByDateDTORequest request;
    
    @BeforeEach
    void setUp() {
        request = new KardexByDateDTORequest();
        
        // Setup context mock behavior using lenient to avoid UnnecessaryStubbingException
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }
    
    @Test
    @DisplayName("Should return true when request is null")
    void testIsValid_NullRequest() {
        // Act
        boolean result = validator.isValid(null, context);
        
        // Assert
        assertTrue(result);
        verify(context, never()).disableDefaultConstraintViolation();
    }
    
    @Test
    @DisplayName("Should return true when both dates are null")
    void testIsValid_BothDatesNull() {
        // Arrange
        request.setStartDate(null);
        request.setEndDate(null);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
        verify(context, never()).disableDefaultConstraintViolation();
    }
    
    @Test
    @DisplayName("Should return true when dates are valid and end is after start")
    void testIsValid_ValidDateRange() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
        verify(context, never()).disableDefaultConstraintViolation();
    }
    
    @Test
    @DisplayName("Should return true when start and end dates are the same")
    void testIsValid_SameDates() {
        // Arrange
        LocalDate sameDate = LocalDate.of(2024, 6, 15);
        request.setStartDate(sameDate);
        request.setEndDate(sameDate);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
        verify(context, never()).disableDefaultConstraintViolation();
    }
    
    @Test
    @DisplayName("Should return false when only start date is null")
    void testIsValid_OnlyStartDateNull() {
        // Arrange
        request.setStartDate(null);
        request.setEndDate(LocalDate.of(2024, 12, 31));
        
        String expectedMessage = "Ambas fechas deben estar presentes o ambas deben ser nulas";
        when(messageService.getMessage(
            eq("kardex.validation.date.range.incomplete"), 
            eq(expectedMessage)
        )).thenReturn(expectedMessage);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertFalse(result);
        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(messageService, times(1)).getMessage(
            "kardex.validation.date.range.incomplete", 
            expectedMessage
        );
        verify(context, times(1)).buildConstraintViolationWithTemplate(expectedMessage);
    }
    
    @Test
    @DisplayName("Should return false when only end date is null")
    void testIsValid_OnlyEndDateNull() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(null);
        
        String expectedMessage = "Ambas fechas deben estar presentes o ambas deben ser nulas";
        when(messageService.getMessage(
            eq("kardex.validation.date.range.incomplete"), 
            eq(expectedMessage)
        )).thenReturn(expectedMessage);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertFalse(result);
        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(messageService, times(1)).getMessage(
            "kardex.validation.date.range.incomplete", 
            expectedMessage
        );
    }
    
    @Test
    @DisplayName("Should return false when end date is before start date")
    void testIsValid_EndBeforeStart() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 12, 31));
        request.setEndDate(LocalDate.of(2024, 1, 1));
        
        String expectedMessage = "La fecha final no puede ser anterior a la fecha inicial";
        when(messageService.getMessage(
            eq("kardex.validation.date.range.invalid"), 
            eq(expectedMessage)
        )).thenReturn(expectedMessage);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertFalse(result);
        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(messageService, times(1)).getMessage(
            "kardex.validation.date.range.invalid", 
            expectedMessage
        );
        verify(context, times(1)).buildConstraintViolationWithTemplate(expectedMessage);
    }
    
    @Test
    @DisplayName("Should return false when end is one day before start")
    void testIsValid_EndOneDayBeforeStart() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 6, 15));
        request.setEndDate(LocalDate.of(2024, 6, 14));
        
        String expectedMessage = "La fecha final no puede ser anterior a la fecha inicial";
        when(messageService.getMessage(
            eq("kardex.validation.date.range.invalid"), 
            eq(expectedMessage)
        )).thenReturn(expectedMessage);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertFalse(result);
        verify(context, times(1)).disableDefaultConstraintViolation();
    }
    
    @Test
    @DisplayName("Should validate correctly with dates spanning one year")
    void testIsValid_OneYearSpan() {
        // Arrange
        request.setStartDate(LocalDate.of(2023, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 1));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should validate correctly with dates spanning multiple years")
    void testIsValid_MultipleYearsSpan() {
        // Arrange
        request.setStartDate(LocalDate.of(2020, 1, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should validate correctly when end is one day after start")
    void testIsValid_EndOneDayAfterStart() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 6, 15));
        request.setEndDate(LocalDate.of(2024, 6, 16));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should use custom message from message service for incomplete range")
    void testIsValid_CustomMessageForIncomplete() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(null);
        
        String customMessage = "Custom: Both dates are required";
        when(messageService.getMessage(
            eq("kardex.validation.date.range.incomplete"), 
            anyString()
        )).thenReturn(customMessage);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertFalse(result);
        verify(context, times(1)).buildConstraintViolationWithTemplate(customMessage);
    }
    
    @Test
    @DisplayName("Should use custom message from message service for invalid range")
    void testIsValid_CustomMessageForInvalid() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 12, 31));
        request.setEndDate(LocalDate.of(2024, 1, 1));
        
        String customMessage = "Custom: End date must be after start date";
        when(messageService.getMessage(
            eq("kardex.validation.date.range.invalid"), 
            anyString()
        )).thenReturn(customMessage);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertFalse(result);
        verify(context, times(1)).buildConstraintViolationWithTemplate(customMessage);
    }
    
    @Test
    @DisplayName("Should validate leap year dates correctly")
    void testIsValid_LeapYearDates() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 2, 29)); // Leap year
        request.setEndDate(LocalDate.of(2024, 3, 1));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should validate year boundary dates correctly")
    void testIsValid_YearBoundaryDates() {
        // Arrange
        request.setStartDate(LocalDate.of(2023, 12, 31));
        request.setEndDate(LocalDate.of(2024, 1, 1));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should fail validation when crossing year boundary backwards")
    void testIsValid_YearBoundaryBackwards() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2023, 12, 31));
        
        String expectedMessage = "La fecha final no puede ser anterior a la fecha inicial";
        when(messageService.getMessage(
            eq("kardex.validation.date.range.invalid"), 
            eq(expectedMessage)
        )).thenReturn(expectedMessage);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should validate minimum valid date range")
    void testIsValid_MinimumValidRange() {
        // Arrange
        LocalDate today = LocalDate.now();
        request.setStartDate(today);
        request.setEndDate(today);
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should validate dates at different months")
    void testIsValid_DifferentMonths() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 3, 15));
        request.setEndDate(LocalDate.of(2024, 9, 20));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should verify violation builder is called with correct message")
    void testIsValid_ViolationBuilderCalled() {
        // Arrange
        request.setStartDate(null);
        request.setEndDate(LocalDate.of(2024, 1, 1));
        
        String expectedMessage = "Test message";
        when(messageService.getMessage(anyString(), anyString())).thenReturn(expectedMessage);
        
        // Act
        validator.isValid(request, context);
        
        // Assert
        verify(violationBuilder, times(1)).addConstraintViolation();
    }
    
    @Test
    @DisplayName("Should handle first day of year")
    void testIsValid_FirstDayOfYear() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 6, 30));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should handle last day of year")
    void testIsValid_LastDayOfYear() {
        // Arrange
        request.setStartDate(LocalDate.of(2024, 6, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));
        
        // Act
        boolean result = validator.isValid(request, context);
        
        // Assert
        assertTrue(result);
    }
}

