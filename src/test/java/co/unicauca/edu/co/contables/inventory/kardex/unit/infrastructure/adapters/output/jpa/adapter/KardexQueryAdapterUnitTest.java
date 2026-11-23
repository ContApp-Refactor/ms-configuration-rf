package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.KardexQueryAdapter;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityQueryMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

/**
 * @brief Unit tests for KardexQueryAdapter
 * 
 * Tests the query operations for Kardex records including
 * pagination, filtering by date, and finding latest records.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexQueryAdapter Tests")
class KardexQueryAdapterUnitTest {

    @Mock
    private IKardexEntityQueryMapper kardexEntityMapper;

    @Mock
    private IKardexRepository kardexRepository;

    @InjectMocks
    private KardexQueryAdapter kardexQueryAdapter;

    private KardexEntity kardexEntity1;
    private KardexEntity kardexEntity2;
    private Kardex kardexDomain1;
    private Kardex kardexDomain2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        // Create first test KardexEntity
        kardexEntity1 = new KardexEntity();
        kardexEntity1.setId(1L);
        kardexEntity1.setProductId(1L);
        kardexEntity1.setFactCode("INV-001");
        kardexEntity1.setQuantity(10);
        kardexEntity1.setUnitPrice(BigDecimal.valueOf(100.00));
        kardexEntity1.setDetails("Test purchase 1");
        kardexEntity1.setType(MovementType.PURCHASE);
        kardexEntity1.setBalanceQuantity(10);
        kardexEntity1.setBalanceUnitPrice(BigDecimal.valueOf(100.00));
        kardexEntity1.setTotalBalance(BigDecimal.valueOf(1000.00));
        kardexEntity1.setDate(ZonedDateTime.now());

        // Create second test KardexEntity
        kardexEntity2 = new KardexEntity();
        kardexEntity2.setId(2L);
        kardexEntity2.setProductId(1L);
        kardexEntity2.setFactCode("INV-002");
        kardexEntity2.setQuantity(5);
        kardexEntity2.setUnitPrice(BigDecimal.valueOf(100.00));
        kardexEntity2.setDetails("Test sale 1");
        kardexEntity2.setType(MovementType.SALE);
        kardexEntity2.setBalanceQuantity(5);
        kardexEntity2.setBalanceUnitPrice(BigDecimal.valueOf(100.00));
        kardexEntity2.setTotalBalance(BigDecimal.valueOf(500.00));
        kardexEntity2.setDate(ZonedDateTime.now());

        // Create domain objects
        kardexDomain1 = new Kardex();
        kardexDomain1.setId(1L);
        kardexDomain1.setProductId(1L);
        kardexDomain1.setFactCode("INV-001");
        kardexDomain1.setType(MovementType.PURCHASE);

        kardexDomain2 = new Kardex();
        kardexDomain2.setId(2L);
        kardexDomain2.setProductId(1L);
        kardexDomain2.setFactCode("INV-002");
        kardexDomain2.setType(MovementType.SALE);
    }

    @Test
    @DisplayName("Should find kardex records by product ID successfully")
    void testFindProductId_Success() {
        // Arrange
        Page<KardexEntity> entityPage = new PageImpl<>(Arrays.asList(kardexEntity1, kardexEntity2));
        when(kardexRepository.findByProductId(eq(1L), any(Pageable.class))).thenReturn(entityPage);
        when(kardexEntityMapper.toDomain(kardexEntity1)).thenReturn(kardexDomain1);
        when(kardexEntityMapper.toDomain(kardexEntity2)).thenReturn(kardexDomain2);

        // Act
        Page<Kardex> result = kardexQueryAdapter.findProductId(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getProductId());
        
        verify(kardexRepository, times(1)).findByProductId(1L, pageable);
        verify(kardexEntityMapper, times(2)).toDomain(any(KardexEntity.class));
    }

    @Test
    @DisplayName("Should find kardex records by product ID and date range successfully")
    void testFindProductIdAndDate_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        Page<KardexEntity> entityPage = new PageImpl<>(Arrays.asList(kardexEntity1));
        when(kardexRepository.findByProductIdAndDateBetween(
            eq(1L), 
            any(ZonedDateTime.class), 
            any(ZonedDateTime.class), 
            any(Pageable.class)
        )).thenReturn(entityPage);
        when(kardexEntityMapper.toDomain(kardexEntity1)).thenReturn(kardexDomain1);

        // Act
        Page<Kardex> result = kardexQueryAdapter.findProductIdAndDate(1L, pageable, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getProductId());
        
        verify(kardexRepository, times(1)).findByProductIdAndDateBetween(
            eq(1L), 
            any(ZonedDateTime.class), 
            any(ZonedDateTime.class), 
            eq(pageable)
        );
    }

    @Test
    @DisplayName("Should find kardex records by fact code, product ID and type successfully")
    void testFindByFactCodeAndProductIdAndType_Success() {
        // Arrange
        List<KardexEntity> entities = Arrays.asList(kardexEntity1);
        when(kardexRepository.findByFactCodeAndProductIdAndType(
            "INV-001", 1L, MovementType.PURCHASE
        )).thenReturn(entities);
        when(kardexEntityMapper.toDomainList(entities)).thenReturn(Arrays.asList(kardexDomain1));

        // Act
        List<Kardex> result = kardexQueryAdapter.findByFactCodeAndProductIdAndType(
            "INV-001", 1L, MovementType.PURCHASE
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("INV-001", result.get(0).getFactCode());
        assertEquals(MovementType.PURCHASE, result.get(0).getType());
        
        verify(kardexRepository, times(1)).findByFactCodeAndProductIdAndType(
            "INV-001", 1L, MovementType.PURCHASE
        );
        verify(kardexEntityMapper, times(1)).toDomainList(entities);
    }

    @Test
    @DisplayName("Should get latest kardex by product ID successfully")
    void testGetLatestKardexByProductId_Success() {
        // Arrange
        when(kardexRepository.findTopByProductIdOrderByDateDesc(1L)).thenReturn(kardexEntity1);
        when(kardexEntityMapper.toDomain(kardexEntity1)).thenReturn(kardexDomain1);

        // Act
        Kardex result = kardexQueryAdapter.getLatestKardexByProductId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getProductId());
        
        verify(kardexRepository, times(1)).findTopByProductIdOrderByDateDesc(1L);
        verify(kardexEntityMapper, times(1)).toDomain(kardexEntity1);
    }

    @Test
    @DisplayName("Should return null when no latest kardex exists")
    void testGetLatestKardexByProductId_NotFound() {
        // Arrange
        when(kardexRepository.findTopByProductIdOrderByDateDesc(999L)).thenReturn(null);

        // Act
        Kardex result = kardexQueryAdapter.getLatestKardexByProductId(999L);

        // Assert
        assertNull(result);
        verify(kardexRepository, times(1)).findTopByProductIdOrderByDateDesc(999L);
        verify(kardexEntityMapper, never()).toDomain(any(KardexEntity.class));
    }

    @Test
    @DisplayName("Should check if kardex exists by fact code, product ID and type")
    void testExistsByFactCodeAndProductIdAndType_True() {
        // Arrange
        when(kardexRepository.existsByFactCodeAndProductIdAndType(
            "INV-001", 1L, MovementType.PURCHASE
        )).thenReturn(true);

        // Act
        boolean result = kardexQueryAdapter.existsByFactCodeAndProductIdAndType(
            "INV-001", 1L, MovementType.PURCHASE
        );

        // Assert
        assertTrue(result);
        verify(kardexRepository, times(1)).existsByFactCodeAndProductIdAndType(
            "INV-001", 1L, MovementType.PURCHASE
        );
    }

    @Test
    @DisplayName("Should return false when kardex does not exist")
    void testExistsByFactCodeAndProductIdAndType_False() {
        // Arrange
        when(kardexRepository.existsByFactCodeAndProductIdAndType(
            "INV-999", 999L, MovementType.SALE
        )).thenReturn(false);

        // Act
        boolean result = kardexQueryAdapter.existsByFactCodeAndProductIdAndType(
            "INV-999", 999L, MovementType.SALE
        );

        // Assert
        assertFalse(result);
        verify(kardexRepository, times(1)).existsByFactCodeAndProductIdAndType(
            "INV-999", 999L, MovementType.SALE
        );
    }

    @Test
    @DisplayName("Should return empty page when no records found")
    void testFindProductId_EmptyResult() {
        // Arrange
        Page<KardexEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(kardexRepository.findByProductId(999L, pageable)).thenReturn(emptyPage);

        // Act
        Page<Kardex> result = kardexQueryAdapter.findProductId(999L, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        
        verify(kardexRepository, times(1)).findByProductId(999L, pageable);
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindProductId_Pagination() {
        // Arrange
        Pageable secondPage = PageRequest.of(1, 5);
        Page<KardexEntity> entityPage = new PageImpl<>(
            Arrays.asList(kardexEntity1), 
            secondPage, 
            10
        );
        when(kardexRepository.findByProductId(1L, secondPage)).thenReturn(entityPage);
        when(kardexEntityMapper.toDomain(kardexEntity1)).thenReturn(kardexDomain1);

        // Act
        Page<Kardex> result = kardexQueryAdapter.findProductId(1L, secondPage);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(10, result.getTotalElements());
        
        verify(kardexRepository, times(1)).findByProductId(1L, secondPage);
    }

    @Test
    @DisplayName("Should handle date range at boundaries correctly")
    void testFindProductIdAndDate_DateBoundaries() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        
        Page<KardexEntity> entityPage = new PageImpl<>(Arrays.asList(kardexEntity1));
        when(kardexRepository.findByProductIdAndDateBetween(
            eq(1L), 
            any(ZonedDateTime.class), 
            any(ZonedDateTime.class), 
            any(Pageable.class)
        )).thenReturn(entityPage);
        when(kardexEntityMapper.toDomain(kardexEntity1)).thenReturn(kardexDomain1);

        // Act
        Page<Kardex> result = kardexQueryAdapter.findProductIdAndDate(1L, pageable, today, tomorrow);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should handle multiple movement types correctly")
    void testFindByFactCodeAndProductIdAndType_DifferentTypes() {
        // Arrange
        when(kardexRepository.findByFactCodeAndProductIdAndType(
            "INV-002", 1L, MovementType.SALE
        )).thenReturn(Arrays.asList(kardexEntity2));
        when(kardexEntityMapper.toDomainList(anyList())).thenReturn(Arrays.asList(kardexDomain2));

        // Act
        List<Kardex> result = kardexQueryAdapter.findByFactCodeAndProductIdAndType(
            "INV-002", 1L, MovementType.SALE
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(MovementType.SALE, result.get(0).getType());
    }

    @Test
    @DisplayName("Should return empty list when no matching records found")
    void testFindByFactCodeAndProductIdAndType_EmptyResult() {
        // Arrange
        when(kardexRepository.findByFactCodeAndProductIdAndType(
            "INV-999", 999L, MovementType.PURCHASERETURN
        )).thenReturn(Collections.emptyList());
        when(kardexEntityMapper.toDomainList(anyList())).thenReturn(Collections.emptyList());

        // Act
        List<Kardex> result = kardexQueryAdapter.findByFactCodeAndProductIdAndType(
            "INV-999", 999L, MovementType.PURCHASERETURN
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle mapper conversion for page content")
    void testMapperConversion_PageContent() {
        // Arrange
        Page<KardexEntity> entityPage = new PageImpl<>(Arrays.asList(kardexEntity1, kardexEntity2));
        when(kardexRepository.findByProductId(1L, pageable)).thenReturn(entityPage);
        when(kardexEntityMapper.toDomain(kardexEntity1)).thenReturn(kardexDomain1);
        when(kardexEntityMapper.toDomain(kardexEntity2)).thenReturn(kardexDomain2);

        // Act
        Page<Kardex> result = kardexQueryAdapter.findProductId(1L, pageable);

        // Assert
        assertNotNull(result);
        verify(kardexEntityMapper, times(1)).toDomain(kardexEntity1);
        verify(kardexEntityMapper, times(1)).toDomain(kardexEntity2);
    }
}
