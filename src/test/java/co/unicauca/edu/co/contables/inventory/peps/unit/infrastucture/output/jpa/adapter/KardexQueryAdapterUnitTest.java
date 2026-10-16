package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.KardexQueryAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IKardexEntityQueryMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IKardexRepository;

@ExtendWith(MockitoExtension.class)
public class KardexQueryAdapterUnitTest {


    @Mock private IKardexEntityQueryMapper kardexEntityQueryMapper;
    @Mock private IKardexRepository kardexRepository;

    @InjectMocks
    private KardexQueryAdapter kardexQueryAdapter;

    private KardexEntity mockEntity;
    private Kardex mockDomain;
    private List<KardexEntity> mockEntities;
    private List<Kardex> mockDomainList;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        mockEntity = createMockEntity(1L);
        mockDomain = createMockDomain(1L);
        mockEntities = createMockEntities();
        mockDomainList = createMockDomainList();
    }

    // ==================== getAllKardex() ====================
    @Test
    @DisplayName("Should get all kardex records by product ID")
    void testGetAllKardex_ValidProductId_ReturnsAllRecords() {
        // Arrange
        Long productId = 1L;
        when(kardexRepository.findByProductId(productId)).thenReturn(mockEntities);
        when(kardexEntityQueryMapper.toDomainList(mockEntities)).thenReturn(mockDomainList);

        // Act
        List<Kardex> result = kardexQueryAdapter.getAllKardex(productId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(kardexRepository).findByProductId(productId);
        verify(kardexEntityQueryMapper).toDomainList(mockEntities);
    }

    @Test
    @DisplayName("Should return empty list when no kardex found")
    void testGetAllKardex_NoRecords_ReturnsEmptyList() {
        // Arrange
        Long productId = 999L;
        when(kardexRepository.findByProductId(productId)).thenReturn(Collections.emptyList());
        when(kardexEntityQueryMapper.toDomainList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // Act
        List<Kardex> result = kardexQueryAdapter.getAllKardex(productId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(kardexRepository).findByProductId(productId);
    }

    // ==================== getAvailableAmountByProduct() ====================
    @Test
    @DisplayName("Should get total available amount by product")
    void testGetAvailableAmountByProduct_ValidProduct_ReturnsSum() {
        // Arrange
        Long productId = 1L;
        when(kardexRepository.sumAvailableAmountByProduct(productId)).thenReturn(500);

        // Act
        int result = kardexQueryAdapter.getAvailableAmountByProduct(productId);

        // Assert
        assertEquals(500, result);
        verify(kardexRepository).sumAvailableAmountByProduct(productId);
    }

    @Test
    @DisplayName("Should return 0 when no available amount")
    void testGetAvailableAmountByProduct_NoStock_ReturnsZero() {
        // Arrange
        Long productId = 999L;
        when(kardexRepository.sumAvailableAmountByProduct(productId)).thenReturn(0);

        // Act
        int result = kardexQueryAdapter.getAvailableAmountByProduct(productId);

        // Assert
        assertEquals(0, result);
        verify(kardexRepository).sumAvailableAmountByProduct(productId);
    }

    // ==================== getFirstRecordByAmountAvailable() ====================
    @Test
    @DisplayName("Should get first record with available amount")
    void testGetFirstRecordByAmountAvailable_RecordExists_ReturnsOptional() {
        // Arrange
        Long productId = 1L;
        when(kardexRepository.findFirstByAvailableAmount(productId))
            .thenReturn(Optional.of(mockEntity));
        when(kardexEntityQueryMapper.toDomain(mockEntity)).thenReturn(mockDomain);

        // Act
        Optional<Kardex> result = kardexQueryAdapter.getFirstRecordByAmountAvailable(productId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getIdKardex());
        verify(kardexRepository).findFirstByAvailableAmount(productId);
        verify(kardexEntityQueryMapper).toDomain(mockEntity);
    }

    @Test
    @DisplayName("Should return empty optional when no record found")
    void testGetFirstRecordByAmountAvailable_NoRecord_ReturnsEmpty() {
        // Arrange
        Long productId = 999L;
        when(kardexRepository.findFirstByAvailableAmount(productId))
            .thenReturn(Optional.empty());

        // Act
        Optional<Kardex> result = kardexQueryAdapter.getFirstRecordByAmountAvailable(productId);

        // Assert
        assertFalse(result.isPresent());
        verify(kardexRepository).findFirstByAvailableAmount(productId);
        verify(kardexEntityQueryMapper, never()).toDomain(any());
    }

    // ==================== findMovementsByProductAndDateRange() ====================
    @Test
    @DisplayName("Should find movements by product and date range")
    void testFindMovementsByProductAndDateRange_ValidRange_ReturnsMovements() {
        // Arrange
        Long productId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        
        when(kardexRepository.findMovementsInDateRange(eq(productId), any(ZonedDateTime.class), any(ZonedDateTime.class)))
            .thenReturn(mockEntities);
        when(kardexEntityQueryMapper.toDomain(any(KardexEntity.class)))
            .thenReturn(mockDomain);

        // Act
        List<Kardex> result = kardexQueryAdapter.findMovementsByProductAndDateRange(productId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        ArgumentCaptor<ZonedDateTime> startCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        ArgumentCaptor<ZonedDateTime> endCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        verify(kardexRepository).findMovementsInDateRange(eq(productId), startCaptor.capture(), endCaptor.capture());
        
        // Verify date conversion
        assertEquals(startDate, startCaptor.getValue().toLocalDate());
        assertEquals(endDate.plusDays(1), endCaptor.getValue().toLocalDate());
    }

    @Test
    @DisplayName("Should return empty list when no movements in range")
    void testFindMovementsByProductAndDateRange_NoMovements_ReturnsEmpty() {
        // Arrange
        Long productId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        
        when(kardexRepository.findMovementsInDateRange(eq(productId), any(ZonedDateTime.class), any(ZonedDateTime.class)))
            .thenReturn(Collections.emptyList());

        // Act
        List<Kardex> result = kardexQueryAdapter.findMovementsByProductAndDateRange(productId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== findMovementsByProductBeforeDate() ====================
    @Test
    @DisplayName("Should find movements before specific date")
    void testFindMovementsByProductBeforeDate_ValidDate_ReturnsMovements() {
        // Arrange
        Long productId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 15);
        
        when(kardexRepository.findMovementsBeforeDate(eq(productId), any(ZonedDateTime.class)))
            .thenReturn(mockEntities);
        when(kardexEntityQueryMapper.toDomain(any(KardexEntity.class)))
            .thenReturn(mockDomain);

        // Act
        List<Kardex> result = kardexQueryAdapter.findMovementsByProductBeforeDate(productId, startDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(kardexRepository).findMovementsBeforeDate(eq(productId), any(ZonedDateTime.class));
    }

    @Test
    @DisplayName("Should return empty list when no movements before date")
    void testFindMovementsByProductBeforeDate_NoMovements_ReturnsEmpty() {
        // Arrange
        Long productId = 1L;
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        
        when(kardexRepository.findMovementsBeforeDate(eq(productId), any(ZonedDateTime.class)))
            .thenReturn(Collections.emptyList());

        // Act
        List<Kardex> result = kardexQueryAdapter.findMovementsByProductBeforeDate(productId, startDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== findByRefFacture() ====================
    @Test
    @DisplayName("Should find kardex by reference facture and product")
    void testFindByRefFacture_ValidReference_ReturnsOptional() {
        // Arrange
        String factCode = "1001";
        Long productId = 1L;
        
        when(kardexRepository.findFirstByFactCodeAndProduct_ProductIdOrderByDateAsc(factCode, productId))
            .thenReturn(Optional.of(mockEntity));
        when(kardexEntityQueryMapper.toDomain(mockEntity)).thenReturn(mockDomain);

        // Act
        Optional<Kardex> result = kardexQueryAdapter.findByRefFacture(factCode, productId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1001", result.get().getFactCode());
        verify(kardexRepository).findFirstByFactCodeAndProduct_ProductIdOrderByDateAsc(factCode, productId);
        verify(kardexEntityQueryMapper).toDomain(mockEntity);
    }

    @Test
    @DisplayName("Should return empty when reference not found")
    void testFindByRefFacture_NotFound_ReturnsEmpty() {
        // Arrange
        String factCode = "9999";
        Long productId = 1L;
        
        when(kardexRepository.findFirstByFactCodeAndProduct_ProductIdOrderByDateAsc(factCode, productId))
            .thenReturn(Optional.empty());

        // Act
        Optional<Kardex> result = kardexQueryAdapter.findByRefFacture(factCode, productId);

        // Assert
        assertFalse(result.isPresent());
        verify(kardexEntityQueryMapper, never()).toDomain(any());
    }

    // ==================== findAvailablePurchasesOrderedByDate() ====================
    @Test
    @DisplayName("Should find available purchases ordered by date")
    void testFindAvailablePurchasesOrderedByDate_ValidProduct_ReturnsOrdered() {
        // Arrange
        Long productId = 1L;
        when(kardexRepository.findAvailablePurchasesOrderedByDate(productId))
            .thenReturn(mockEntities);
        when(kardexEntityQueryMapper.toDomainList(mockEntities)).thenReturn(mockDomainList);

        // Act
        List<Kardex> result = kardexQueryAdapter.findAvailablePurchasesOrderedByDate(productId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(kardexRepository).findAvailablePurchasesOrderedByDate(productId);
        verify(kardexEntityQueryMapper).toDomainList(mockEntities);
    }

    @Test
    @DisplayName("Should return empty list when no available purchases")
    void testFindAvailablePurchasesOrderedByDate_NoAvailable_ReturnsEmpty() {
        // Arrange
        Long productId = 999L;
        when(kardexRepository.findAvailablePurchasesOrderedByDate(productId))
            .thenReturn(Collections.emptyList());
        when(kardexEntityQueryMapper.toDomainList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // Act
        List<Kardex> result = kardexQueryAdapter.findAvailablePurchasesOrderedByDate(productId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== findById() ====================
    @Test
    @DisplayName("Should find kardex by ID")
    void testFindById_ValidId_ReturnsOptional() {
        // Arrange
        Long kardexId = 1L;
        when(kardexRepository.findById(kardexId)).thenReturn(Optional.of(mockEntity));
        when(kardexEntityQueryMapper.toDomain(mockEntity)).thenReturn(mockDomain);

        // Act
        Optional<Kardex> result = kardexQueryAdapter.findById(kardexId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getIdKardex());
        verify(kardexRepository).findById(kardexId);
        verify(kardexEntityQueryMapper).toDomain(mockEntity);
    }

    @Test
    @DisplayName("Should return empty when ID not found")
    void testFindById_NotFound_ReturnsEmpty() {
        // Arrange
        Long kardexId = 999L;
        when(kardexRepository.findById(kardexId)).thenReturn(Optional.empty());

        // Act
        Optional<Kardex> result = kardexQueryAdapter.findById(kardexId);

        // Assert
        assertFalse(result.isPresent());
        verify(kardexRepository).findById(kardexId);
        verify(kardexEntityQueryMapper, never()).toDomain(any());
    }

    // ==================== Helper Methods ====================
    private KardexEntity createMockEntity(Long id) {
        KardexEntity entity = new KardexEntity();
        entity.setIdKardex(id);
        entity.setFactCode("1001");
        entity.setQuantity(100);
        entity.setAvailableQuantity(80);
        entity.setUnitPrice(new BigDecimal("10.00"));
        return entity;
    }

    private Kardex createMockDomain(Long id) {
        return Kardex.builder()
            .idKardex(id)
            .factCode("1001")
            .quantity(100)
            .availableQuantity(80)
            .unitPrice(new BigDecimal("10.00"))
            .build();
    }

    private List<KardexEntity> createMockEntities() {
        List<KardexEntity> entities = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            entities.add(createMockEntity(i));
        }
        return entities;
    }

    private List<Kardex> createMockDomainList() {
        List<Kardex> domains = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            domains.add(createMockDomain(i));
        }
        return domains;
    }
    
}
