package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.KardexCommandAdapter;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityCommandMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

/**
 * @brief Unit tests for KardexCommandAdapter
 * 
 * Tests the write operations for Kardex records including
 * purchases, sales, and returns.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexCommandAdapter Tests")
class KardexCommandAdapterUnitTest {

    @Mock
    private IKardexEntityCommandMapper kardexEntityMapper;

    @Mock
    private IKardexRepository kardexRepository;

    @InjectMocks
    private KardexCommandAdapter kardexCommandAdapter;

    private Kardex kardexDomain;
    private KardexEntity kardexEntity;
    private KardexEntity savedKardexEntity;

    @BeforeEach
    void setUp() {
        // Create test Kardex domain object
        kardexDomain = new Kardex();
        kardexDomain.setProductId(1L);
        kardexDomain.setFactCode("INV-001");
        kardexDomain.setQuantity(10);
        kardexDomain.setUnitPrice(BigDecimal.valueOf(100.00));
        kardexDomain.setDetails("Test purchase");
        kardexDomain.setType(MovementType.PURCHASE);
        kardexDomain.setBalanceQuantity(10);
        kardexDomain.setBalanceUnitPrice(BigDecimal.valueOf(100.00));
        kardexDomain.setTotalBalance(BigDecimal.valueOf(1000.00));
        kardexDomain.setDate(ZonedDateTime.now());

        // Create test KardexEntity
        kardexEntity = new KardexEntity();
        kardexEntity.setProductId(1L);
        kardexEntity.setFactCode("INV-001");
        kardexEntity.setQuantity(10);
        kardexEntity.setUnitPrice(BigDecimal.valueOf(100.00));
        kardexEntity.setDetails("Test purchase");
        kardexEntity.setType(MovementType.PURCHASE);
        kardexEntity.setBalanceQuantity(10);
        kardexEntity.setBalanceUnitPrice(BigDecimal.valueOf(100.00));
        kardexEntity.setTotalBalance(BigDecimal.valueOf(1000.00));
        kardexEntity.setDate(ZonedDateTime.now());

        // Create saved entity with ID
        savedKardexEntity = new KardexEntity();
        savedKardexEntity.setId(1L);
        savedKardexEntity.setProductId(1L);
        savedKardexEntity.setFactCode("INV-001");
        savedKardexEntity.setQuantity(10);
        savedKardexEntity.setUnitPrice(BigDecimal.valueOf(100.00));
        savedKardexEntity.setDetails("Test purchase");
        savedKardexEntity.setType(MovementType.PURCHASE);
        savedKardexEntity.setBalanceQuantity(10);
        savedKardexEntity.setBalanceUnitPrice(BigDecimal.valueOf(100.00));
        savedKardexEntity.setTotalBalance(BigDecimal.valueOf(1000.00));
        savedKardexEntity.setDate(ZonedDateTime.now());
    }

    @Test
    @DisplayName("Should register purchase successfully")
    void testRegisterPurchase_Success() {
        // Arrange
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerPurchase(kardexDomain);

        // Assert
        assertNotNull(result);
        assertEquals(kardexDomain.getProductId(), result.getProductId());
        assertEquals(kardexDomain.getFactCode(), result.getFactCode());
        assertEquals(MovementType.PURCHASE, result.getType());
        
        verify(kardexEntityMapper, times(1)).toEntity(kardexDomain);
        verify(kardexRepository, times(1)).save(kardexEntity);
        verify(kardexEntityMapper, times(1)).toDomain(savedKardexEntity);
    }

    @Test
    @DisplayName("Should register sale successfully")
    void testRegisterSale_Success() {
        // Arrange
        kardexDomain.setType(MovementType.SALE);
        kardexEntity.setType(MovementType.SALE);
        savedKardexEntity.setType(MovementType.SALE);
        
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerSale(kardexDomain);

        // Assert
        assertNotNull(result);
        assertEquals(kardexDomain.getProductId(), result.getProductId());
        assertEquals(MovementType.SALE, result.getType());
        
        verify(kardexEntityMapper, times(1)).toEntity(kardexDomain);
        verify(kardexRepository, times(1)).save(kardexEntity);
        verify(kardexEntityMapper, times(1)).toDomain(savedKardexEntity);
    }

    @Test
    @DisplayName("Should register return on purchase successfully")
    void testRegisterReturnOnPurchase_Success() {
        // Arrange
        kardexDomain.setType(MovementType.PURCHASERETURN);
        kardexEntity.setType(MovementType.PURCHASERETURN);
        savedKardexEntity.setType(MovementType.PURCHASERETURN);
        
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerReturnOnPurchase(kardexDomain);

        // Assert
        assertNotNull(result);
        assertEquals(kardexDomain.getProductId(), result.getProductId());
        assertEquals(MovementType.PURCHASERETURN, result.getType());
        
        verify(kardexEntityMapper, times(1)).toEntity(kardexDomain);
        verify(kardexRepository, times(1)).save(kardexEntity);
        verify(kardexEntityMapper, times(1)).toDomain(savedKardexEntity);
    }

    @Test
    @DisplayName("Should register return on sale successfully")
    void testRegisterReturnOnSale_Success() {
        // Arrange
        kardexDomain.setType(MovementType.SALESRETURN);
        kardexEntity.setType(MovementType.SALESRETURN);
        savedKardexEntity.setType(MovementType.SALESRETURN);
        
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerReturnOnSale(kardexDomain);

        // Assert
        assertNotNull(result);
        assertEquals(kardexDomain.getProductId(), result.getProductId());
        assertEquals(MovementType.SALESRETURN, result.getType());
        
        verify(kardexEntityMapper, times(1)).toEntity(kardexDomain);
        verify(kardexRepository, times(1)).save(kardexEntity);
        verify(kardexEntityMapper, times(1)).toDomain(savedKardexEntity);
    }

    @Test
    @DisplayName("Should delete all kardex records successfully")
    void testDeleteAll_Success() {
        // Arrange
        doNothing().when(kardexRepository).deleteAll();

        // Act
        kardexCommandAdapter.deleteAll();

        // Assert
        verify(kardexRepository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("Should handle mapper conversion correctly for purchase")
    void testMapperConversion_Purchase() {
        // Arrange
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerPurchase(kardexDomain);

        // Assert
        assertNotNull(result);
        verify(kardexEntityMapper).toEntity(kardexDomain);
        verify(kardexEntityMapper).toDomain(savedKardexEntity);
    }

    @Test
    @DisplayName("Should persist entity with correct attributes")
    void testPersistEntityAttributes() {
        // Arrange
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        kardexCommandAdapter.registerPurchase(kardexDomain);

        // Assert
        verify(kardexRepository).save(argThat(entity -> 
            entity.getProductId().equals(1L) &&
            entity.getFactCode().equals("INV-001") &&
            entity.getQuantity() == 10 &&
            entity.getType() == MovementType.PURCHASE
        ));
    }

    @Test
    @DisplayName("Should handle different product IDs correctly")
    void testDifferentProductIds() {
        // Arrange
        kardexDomain.setProductId(999L);
        kardexEntity.setProductId(999L);
        savedKardexEntity.setProductId(999L);
        
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerPurchase(kardexDomain);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getProductId());
    }

    @Test
    @DisplayName("Should handle large quantities correctly")
    void testLargeQuantities() {
        // Arrange
        kardexDomain.setQuantity(10000);
        kardexEntity.setQuantity(10000);
        savedKardexEntity.setQuantity(10000);
        
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerSale(kardexDomain);

        // Assert
        assertNotNull(result);
        assertEquals(10000, result.getQuantity());
    }

    @Test
    @DisplayName("Should handle decimal prices correctly")
    void testDecimalPrices() {
        // Arrange
        BigDecimal precisePrice = new BigDecimal("99.99");
        kardexDomain.setUnitPrice(precisePrice);
        kardexEntity.setUnitPrice(precisePrice);
        savedKardexEntity.setUnitPrice(precisePrice);
        
        when(kardexEntityMapper.toEntity(any(Kardex.class))).thenReturn(kardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(savedKardexEntity);
        when(kardexEntityMapper.toDomain(any(KardexEntity.class))).thenReturn(kardexDomain);

        // Act
        Kardex result = kardexCommandAdapter.registerPurchase(kardexDomain);

        // Assert
        assertNotNull(result);
        assertEquals(precisePrice, result.getUnitPrice());
    }
}
