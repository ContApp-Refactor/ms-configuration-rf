package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.application.service.command.KardexAdjustmentValidationService;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IConfigClientPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IKardexQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;

/**
 * @brief Unit tests for KardexAdjustmentValidationService
 * 
 * Tests validation logic for kardex adjustments including date validation,
 * accounting period validation, and business rule enforcement.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kardex Adjustment Validation Service Unit Tests")
class KardexAdjustmentValidationServiceUnitTest {
    
    @Mock
    private IConfigClientPort configClientPort;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IMessageServicePort messageServicePort;
    
    @Mock
    private IKardexQueryOutputPort kardexQueryOutputPort;
    
    @InjectMocks
    private KardexAdjustmentValidationService validationService;
    
    private Kardex kardex;
    private Product product;
    private String enterpriseId;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        
        product = Product.builder()
                .productId(1L)
                .name("Test Product")
                .build();
        
        kardex = Kardex.builder()
                .product(product)
                .quantity(10)
                .build();
    }
    
    @Test
    @DisplayName("Should set current date when date is null")
    void testValidateDateForAdjustment_WhenDateIsNull_ShouldSetCurrentDate() {
        // Arrange
        kardex.setDate(null);
        when(kardexQueryOutputPort.getLatestKardexByProductId(anyLong())).thenReturn(null);
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class))).thenReturn(true);
        
        // Act
        validationService.validateDateForAdjustment(kardex, enterpriseId);
        
        // Assert
        assertNotNull(kardex.getDate(), "Date should be set");
        assertTrue(kardex.getDate().getZone().getId().equals("America/Bogota"), 
                   "Zone should be America/Bogota");
    }
    
    @Test
    @DisplayName("Should throw error when date is in the future")
    void testValidateDateForAdjustment_WhenDateIsInFuture_ShouldThrowError() {
        // Arrange
        ZonedDateTime futureDate = ZonedDateTime.now(ZoneId.of("America/Bogota")).plusDays(1);
        kardex.setDate(futureDate);
        String errorMessage = "Date cannot be in the future";
        
        when(messageServicePort.getMessage(MessageKeys.DATE_CANNOT_BE_FUTURE))
                .thenReturn(errorMessage);
        doThrow(new RuntimeException(errorMessage))
                .when(formatterResultOutputPort)
                .returnBusinessRuleErrorResponse(eq(400), eq(errorMessage));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            validationService.validateDateForAdjustment(kardex, enterpriseId),
            "Should throw exception for future date"
        );
        
        verify(messageServicePort).getMessage(MessageKeys.DATE_CANNOT_BE_FUTURE);
        verify(formatterResultOutputPort).returnBusinessRuleErrorResponse(400, errorMessage);
    }
    
    @Test
    @DisplayName("Should allow adjustment when no previous kardex exists")
    void testValidateDateForAdjustment_WhenNoPreviousKardex_ShouldAllow() {
        // Arrange
        ZonedDateTime currentDate = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        kardex.setDate(currentDate);
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(null);
        when(configClientPort.isValidAccountingDate(enterpriseId, currentDate.toLocalDate()))
                .thenReturn(true);
        
        // Act
        validationService.validateDateForAdjustment(kardex, enterpriseId);
        
        // Assert
        verify(kardexQueryOutputPort).getLatestKardexByProductId(product.getProductId());
        verify(configClientPort).isValidAccountingDate(enterpriseId, currentDate.toLocalDate());
        verifyNoInteractions(formatterResultOutputPort);
    }
    
    @Test
    @DisplayName("Should adjust time when date is same day as last record")
    void testValidateDateForAdjustment_WhenSameDayAsLastRecord_ShouldAddOneSecond() {
        // Arrange
        ZonedDateTime lastKardexDate = ZonedDateTime.now(ZoneId.of("America/Bogota"))
                .withHour(10).withMinute(0).withSecond(0);
        ZonedDateTime adjustmentDate = lastKardexDate.withHour(14);
        
        kardex.setDate(adjustmentDate);
        
        Kardex lastKardex = Kardex.builder()
                .idKardex(1L)
                .date(lastKardexDate)
                .product(product)
                .build();
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(lastKardex);
        when(configClientPort.isValidAccountingDate(anyString(), any(LocalDate.class)))
                .thenReturn(true);
        
        // Act
        validationService.validateDateForAdjustment(kardex, enterpriseId);
        
        // Assert
        assertEquals(lastKardexDate.plusSeconds(1), kardex.getDate(),
                     "Date should be last record date plus 1 second");
        verify(kardexQueryOutputPort).getLatestKardexByProductId(product.getProductId());
    }
    
    @Test
    @DisplayName("Should throw error when date is before last record and not same day")
    void testValidateDateForAdjustment_WhenDateBeforeLastRecord_ShouldThrowError() {
        // Arrange
        ZonedDateTime lastKardexDate = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        ZonedDateTime adjustmentDate = lastKardexDate.minusDays(2);
        
        kardex.setDate(adjustmentDate);
        
        Kardex lastKardex = Kardex.builder()
                .idKardex(1L)
                .date(lastKardexDate)
                .product(product)
                .build();
        
        String errorMessage = "Date cannot be before last record";
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(lastKardex);
        when(messageServicePort.getMessage(MessageKeys.DATE_CANNOT_BE_BEFORE_LAST_RECORD))
                .thenReturn(errorMessage);
        doThrow(new RuntimeException(errorMessage))
                .when(formatterResultOutputPort)
                .returnBusinessRuleErrorResponse(eq(400), eq(errorMessage));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            validationService.validateDateForAdjustment(kardex, enterpriseId),
            "Should throw exception for date before last record"
        );
        
        verify(messageServicePort).getMessage(MessageKeys.DATE_CANNOT_BE_BEFORE_LAST_RECORD);
        verify(formatterResultOutputPort).returnBusinessRuleErrorResponse(400, errorMessage);
    }
    
    @Test
    @DisplayName("Should allow date after last record on different day")
    void testValidateDateForAdjustment_WhenDateAfterLastRecord_ShouldAllow() {
        // Arrange
        ZonedDateTime lastKardexDate = ZonedDateTime.now(ZoneId.of("America/Bogota")).minusDays(5);
        ZonedDateTime adjustmentDate = ZonedDateTime.now(ZoneId.of("America/Bogota")).minusDays(2);
        
        kardex.setDate(adjustmentDate);
        
        Kardex lastKardex = Kardex.builder()
                .idKardex(1L)
                .date(lastKardexDate)
                .product(product)
                .build();
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(lastKardex);
        when(configClientPort.isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate()))
                .thenReturn(true);
        
        // Act
        validationService.validateDateForAdjustment(kardex, enterpriseId);
        
        // Assert
        assertEquals(adjustmentDate, kardex.getDate(), "Date should remain unchanged");
        verify(kardexQueryOutputPort).getLatestKardexByProductId(product.getProductId());
        verify(configClientPort).isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate());
    }
    
    @Test
    @DisplayName("Should throw error when accounting date is invalid")
    void testValidateDateForAdjustment_WhenAccountingDateInvalid_ShouldThrowError() {
        // Arrange
        ZonedDateTime adjustmentDate = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        kardex.setDate(adjustmentDate);
        
        String errorMessage = "Invalid accounting date";
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(null);
        when(configClientPort.isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate()))
                .thenReturn(false);
        when(messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE))
                .thenReturn(errorMessage);
        doThrow(new RuntimeException(errorMessage))
                .when(formatterResultOutputPort)
                .returnErrorGenericResponse(eq(400), eq(errorMessage));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            validationService.validateDateForAdjustment(kardex, enterpriseId),
            "Should throw exception for invalid accounting date"
        );
        
        verify(configClientPort).isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate());
        verify(messageServicePort).getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
        verify(formatterResultOutputPort).returnErrorGenericResponse(400, errorMessage);
    }
    
    @Test
    @DisplayName("Should validate successfully with valid date and accounting period")
    void testValidateDateForAdjustment_WithValidDateAndAccountingPeriod_ShouldPass() {
        // Arrange
        ZonedDateTime lastKardexDate = ZonedDateTime.now(ZoneId.of("America/Bogota")).minusDays(5);
        ZonedDateTime adjustmentDate = ZonedDateTime.now(ZoneId.of("America/Bogota")).minusDays(1);
        
        kardex.setDate(adjustmentDate);
        
        Kardex lastKardex = Kardex.builder()
                .idKardex(1L)
                .date(lastKardexDate)
                .product(product)
                .build();
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(lastKardex);
        when(configClientPort.isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate()))
                .thenReturn(true);
        
        // Act
        validationService.validateDateForAdjustment(kardex, enterpriseId);
        
        // Assert
        verify(kardexQueryOutputPort).getLatestKardexByProductId(product.getProductId());
        verify(configClientPort).isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate());
        verifyNoMoreInteractions(formatterResultOutputPort);
    }
    
    @Test
    @DisplayName("Should handle last kardex with null date")
    void testValidateDateForAdjustment_WhenLastKardexHasNullDate_ShouldSkipDateComparison() {
        // Arrange
        ZonedDateTime adjustmentDate = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        kardex.setDate(adjustmentDate);
        
        Kardex lastKardex = Kardex.builder()
                .idKardex(1L)
                .date(null)  // Null date
                .product(product)
                .build();
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(lastKardex);
        when(configClientPort.isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate()))
                .thenReturn(true);
        
        // Act
        validationService.validateDateForAdjustment(kardex, enterpriseId);
        
        // Assert
        assertEquals(adjustmentDate, kardex.getDate(), "Date should remain unchanged");
        verify(kardexQueryOutputPort).getLatestKardexByProductId(product.getProductId());
        verify(configClientPort).isValidAccountingDate(enterpriseId, adjustmentDate.toLocalDate());
    }
    
    @Test
    @DisplayName("Should handle current datetime validation")
    void testValidateDateForAdjustment_WithCurrentDateTime_ShouldPass() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        kardex.setDate(now);
        
        when(kardexQueryOutputPort.getLatestKardexByProductId(product.getProductId()))
                .thenReturn(null);
        when(configClientPort.isValidAccountingDate(enterpriseId, now.toLocalDate()))
                .thenReturn(true);
        
        // Act
        validationService.validateDateForAdjustment(kardex, enterpriseId);
        
        // Assert
        assertNotNull(kardex.getDate());
        verify(configClientPort).isValidAccountingDate(enterpriseId, now.toLocalDate());
    }
    
}
