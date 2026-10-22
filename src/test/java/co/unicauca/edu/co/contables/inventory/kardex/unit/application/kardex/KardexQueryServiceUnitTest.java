package co.unicauca.edu.co.contables.inventory.kardex.unit.application.kardex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.query.KardexQueryService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class KardexQueryServiceUnitTest {
    
    @Mock
    private IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    
    @InjectMocks
    private KardexQueryService kardexQueryService;
    
    private Kardex kardex1;
    private Kardex kardex2;
    private Kardex kardex3;
    private Pageable pageable;
    private List<Kardex> kardexList;
    
    @BeforeEach
    void setUp() {
        // Setup pageable
        pageable = PageRequest.of(0, 10);
        
        // Setup kardex 1 - Purchase
        kardex1 = new Kardex();
        kardex1.setId(1L);
        kardex1.setProductId(1L);
        kardex1.setQuantity(10);
        kardex1.setUnitPrice(new BigDecimal("25.50"));
        kardex1.setBalanceQuantity(10);
        kardex1.setBalanceUnitPrice(new BigDecimal("25.50"));
        kardex1.setTotalBalance(new BigDecimal("255.00"));
        kardex1.setType(MovementType.PURCHASE);
        kardex1.setFactCode("FACT001");
        kardex1.setDate(ZonedDateTime.now().minusDays(5));
        kardex1.setDetails("Compra - Factura: FACT001");
        
        // Setup kardex 2 - Sale
        kardex2 = new Kardex();
        kardex2.setId(2L);
        kardex2.setProductId(1L);
        kardex2.setQuantity(5);
        kardex2.setUnitPrice(new BigDecimal("25.50"));
        kardex2.setBalanceQuantity(5);
        kardex2.setBalanceUnitPrice(new BigDecimal("25.50"));
        kardex2.setTotalBalance(new BigDecimal("127.50"));
        kardex2.setType(MovementType.SALE);
        kardex2.setFactCode("FACT002");
        kardex2.setDate(ZonedDateTime.now().minusDays(3));
        kardex2.setDetails("Venta - Factura: FACT002");
        
        // Setup kardex 3 - Purchase Return
        kardex3 = new Kardex();
        kardex3.setId(3L);
        kardex3.setProductId(1L);
        kardex3.setQuantity(2);
        kardex3.setUnitPrice(new BigDecimal("25.50"));
        kardex3.setBalanceQuantity(3);
        kardex3.setBalanceUnitPrice(new BigDecimal("25.50"));
        kardex3.setTotalBalance(new BigDecimal("76.50"));
        kardex3.setType(MovementType.PURCHASERETURN);
        kardex3.setFactCode("FACT001");
        kardex3.setDate(ZonedDateTime.now().minusDays(1));
        kardex3.setDetails("Devolución de Compra - Factura: FACT001");
        
        kardexList = Arrays.asList(kardex1, kardex2, kardex3);
    }
    
    @Test
    @DisplayName("Should find kardex by product ID without date filters")
    void testFindProductIdWithoutDateFilters() {
        // Arrange
        Page<Kardex> expectedPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        when(kardexQueryRepositoryPort.findProductId(1L, pageable)).thenReturn(expectedPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(1L, pageable, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(kardexList.size(), result.getContent().size());
        verify(kardexQueryRepositoryPort).findProductId(1L, pageable);
        verify(kardexQueryRepositoryPort, never()).findProductIdAndDate(anyLong(), any(Pageable.class), 
                any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    @DisplayName("Should find kardex by product ID with date filters")
    void testFindProductIdWithDateFilters() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Page<Kardex> expectedPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        when(kardexQueryRepositoryPort.findProductIdAndDate(1L, pageable, startDate, endDate))
            .thenReturn(expectedPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(1L, pageable, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(kardexList.size(), result.getContent().size());
        verify(kardexQueryRepositoryPort).findProductIdAndDate(1L, pageable, startDate, endDate);
        verify(kardexQueryRepositoryPort, never()).findProductId(anyLong(), any(Pageable.class));
    }
    
    @Test
    @DisplayName("Should find kardex by product ID with only start date (should not use date filter)")
    void testFindProductIdWithOnlyStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        Page<Kardex> expectedPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        when(kardexQueryRepositoryPort.findProductId(1L, pageable)).thenReturn(expectedPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(1L, pageable, startDate, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(kardexQueryRepositoryPort).findProductId(1L, pageable);
        verify(kardexQueryRepositoryPort, never()).findProductIdAndDate(anyLong(), any(Pageable.class), 
                any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    @DisplayName("Should find kardex by product ID with only end date (should not use date filter)")
    void testFindProductIdWithOnlyEndDate() {
        // Arrange
        LocalDate endDate = LocalDate.now();
        Page<Kardex> expectedPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        when(kardexQueryRepositoryPort.findProductId(1L, pageable)).thenReturn(expectedPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(1L, pageable, null, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(kardexQueryRepositoryPort).findProductId(1L, pageable);
        verify(kardexQueryRepositoryPort, never()).findProductIdAndDate(anyLong(), any(Pageable.class), 
                any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    @DisplayName("Should return empty page when no kardex records exist for product")
    void testFindProductIdWithNoRecords() {
        // Arrange
        Page<Kardex> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(kardexQueryRepositoryPort.findProductId(999L, pageable)).thenReturn(emptyPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(999L, pageable, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(kardexQueryRepositoryPort).findProductId(999L, pageable);
    }
    
    @Test
    @DisplayName("Should return empty page when no kardex records exist in date range")
    void testFindProductIdWithDateFiltersNoRecords() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now().minusYears(1);
        Page<Kardex> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(kardexQueryRepositoryPort.findProductIdAndDate(1L, pageable, startDate, endDate))
            .thenReturn(emptyPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(1L, pageable, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(kardexQueryRepositoryPort).findProductIdAndDate(1L, pageable, startDate, endDate);
    }
    
    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindProductIdWithDifferentPageable() {
        // Arrange
        Pageable customPageable = PageRequest.of(1, 5);
        List<Kardex> secondPageList = Arrays.asList(kardex2, kardex3);
        Page<Kardex> secondPage = new PageImpl<>(secondPageList, customPageable, kardexList.size());
        when(kardexQueryRepositoryPort.findProductId(1L, customPageable)).thenReturn(secondPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(1L, customPageable, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size()); // Records in current page
        assertEquals(1, result.getNumber()); // Current page number
        verify(kardexQueryRepositoryPort).findProductId(1L, customPageable);
    }
    
    @Test
    @DisplayName("Should find kardex with date range spanning multiple months")
    void testFindProductIdWithLongDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(6);
        LocalDate endDate = LocalDate.now();
        Page<Kardex> expectedPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        when(kardexQueryRepositoryPort.findProductIdAndDate(1L, pageable, startDate, endDate))
            .thenReturn(expectedPage);
        
        // Act
        Page<Kardex> result = kardexQueryService.findProductId(1L, pageable, startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(kardexQueryRepositoryPort).findProductIdAndDate(1L, pageable, startDate, endDate);
    }
    
    @Test
    @DisplayName("Should find kardex for different product IDs independently")
    void testFindDifferentProductIds() {
        // Arrange
        Long productId1 = 1L;
        Long productId2 = 2L;
        Page<Kardex> page1 = new PageImpl<>(Arrays.asList(kardex1), pageable, 1);
        Page<Kardex> page2 = new PageImpl<>(Arrays.asList(kardex2), pageable, 1);
        
        when(kardexQueryRepositoryPort.findProductId(productId1, pageable)).thenReturn(page1);
        when(kardexQueryRepositoryPort.findProductId(productId2, pageable)).thenReturn(page2);
        
        // Act
        Page<Kardex> result1 = kardexQueryService.findProductId(productId1, pageable, null, null);
        Page<Kardex> result2 = kardexQueryService.findProductId(productId2, pageable, null, null);
        
        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.getTotalElements());
        assertEquals(1, result2.getTotalElements());
        verify(kardexQueryRepositoryPort).findProductId(productId1, pageable);
        verify(kardexQueryRepositoryPort).findProductId(productId2, pageable);
    }
}
