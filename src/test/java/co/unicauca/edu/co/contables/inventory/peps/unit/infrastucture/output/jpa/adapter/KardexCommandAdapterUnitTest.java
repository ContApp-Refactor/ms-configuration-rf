package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.KardexCommandAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IKardexEntityCommandMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IDetailOutPutRepository;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository;

@ExtendWith(MockitoExtension.class)
public class KardexCommandAdapterUnitTest {
    
    @Mock private IKardexEntityCommandMapper kardexEntityCommandMapper;
    @Mock private IKardexRepository kardexRepository;
    @Mock private IDetailOutPutRepository detailOutPutRepository;
    @Mock private IProductRepository productRepository;

    @InjectMocks
    private KardexCommandAdapter kardexCommandAdapter;

    private Product mockProduct;
    private ProductEntity mockProductEntity;
    private Kardex mockKardex;
    private KardexEntity mockKardexEntity;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setProductId(100L);
        mockProduct.setName("Test Product");

        mockProductEntity = new ProductEntity();
        mockProductEntity.setId(1L);
        mockProductEntity.setProductId(100L);

        mockKardex = Kardex.builder()
            .idKardex(1L)
            .factCode("1001")
            .quantity(100)
            .unitPrice(new BigDecimal("10.00"))
            .product(mockProduct)
            .build();

        mockKardexEntity = new KardexEntity();
        mockKardexEntity.setIdKardex(1L);
        mockKardexEntity.setFactCode("1001");
        mockKardexEntity.setQuantity(100);
    }

    // ==================== registerPurchase() ====================
    @Test
    @DisplayName("Should register purchase successfully")
    void testRegisterPurchase_ValidKardex_ReturnsRegisteredPurchase() {
        // Arrange
        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(mockKardexEntity)).thenReturn(mockKardexEntity);
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        Kardex result = kardexCommandAdapter.registerPurchase(mockKardex);

        // Assert
        assertNotNull(result);
        assertEquals("1001", result.getFactCode());
        verify(productRepository).getReferenceById(1L);
        verify(kardexRepository).save(mockKardexEntity);
        verify(kardexEntityCommandMapper).toDomain(mockKardexEntity);
    }

    @Test
    @DisplayName("Should set product entity when registering purchase")
    void testRegisterPurchase_ValidData_SetsProductEntity() {
        // Arrange
        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(mockKardexEntity)).thenReturn(mockKardexEntity);
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        kardexCommandAdapter.registerPurchase(mockKardex);

        // Assert
        ArgumentCaptor<KardexEntity> captor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(captor.capture());
        assertNotNull(captor.getValue().getProduct());
    }

    // ==================== registerSale() ====================
    @Test
    @DisplayName("Should register sale and update lots")
    void testRegisterSale_ValidSaleWithLots_RegistersAndUpdatesLots() {
        // Arrange
        Kardex lot1 = Kardex.builder().idKardex(10L).product(mockProduct).build();
        Kardex lot2 = Kardex.builder().idKardex(11L).product(mockProduct).build();
        List<Kardex> lotsToUpdate = List.of(lot1, lot2);

        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(50);
        detail.setUnitPrice(new BigDecimal("10.00"));
        Kardex originMovement = Kardex.builder().idKardex(10L).build();
        detail.setMovementOrigin(originMovement);
        mockKardex.setDetailsOutput(List.of(detail));

        KardexEntity lotEntity1 = new KardexEntity();
        KardexEntity lotEntity2 = new KardexEntity();
        KardexEntity originEntity = new KardexEntity();

        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(lot1)).thenReturn(lotEntity1);
        when(kardexEntityCommandMapper.toEntity(lot2)).thenReturn(lotEntity2);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(mockKardexEntity);
        when(kardexRepository.getReferenceById(10L)).thenReturn(originEntity);
        when(detailOutPutRepository.save(any(DetailOutputEntity.class)))
            .thenReturn(new DetailOutputEntity());
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        Kardex result = kardexCommandAdapter.registerSale(mockKardex, lotsToUpdate);

        // Assert
        assertNotNull(result);
        verify(kardexRepository, times(3)).save(any(KardexEntity.class)); // 2 lots + 1 sale
        verify(detailOutPutRepository).save(any(DetailOutputEntity.class));
    }

    @Test
    @DisplayName("Should save sale details correctly")
    void testRegisterSale_WithDetails_SavesDetailsCorrectly() {
        // Arrange
        DetailOutput detail1 = new DetailOutput();
        detail1.setQuantityUsed(30);
        detail1.setUnitPrice(new BigDecimal("10.00"));
        Kardex origin1 = Kardex.builder().idKardex(10L).build();
        detail1.setMovementOrigin(origin1);

        DetailOutput detail2 = new DetailOutput();
        detail2.setQuantityUsed(20);
        detail2.setUnitPrice(new BigDecimal("12.00"));
        Kardex origin2 = Kardex.builder().idKardex(11L).build();
        detail2.setMovementOrigin(origin2);

        mockKardex.setDetailsOutput(List.of(detail1, detail2));

        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(mockKardexEntity)).thenReturn(mockKardexEntity);
        when(kardexRepository.getReferenceById(anyLong())).thenReturn(new KardexEntity());
        when(detailOutPutRepository.save(any(DetailOutputEntity.class)))
            .thenReturn(new DetailOutputEntity());
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        kardexCommandAdapter.registerSale(mockKardex, new ArrayList<>());

        // Assert
        verify(detailOutPutRepository, times(2)).save(any(DetailOutputEntity.class));
    }

    // ==================== registerPurchaseReturn() ====================
    @Test
    @DisplayName("Should register purchase return successfully")
    void testRegisterPurchaseReturn_ValidReturn_RegistersSuccessfully() {
        // Arrange
        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(mockKardexEntity)).thenReturn(mockKardexEntity);
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        Kardex result = kardexCommandAdapter.registerPurchaseReturn(mockKardex);

        // Assert
        assertNotNull(result);
        verify(productRepository).getReferenceById(1L);
        verify(kardexRepository).save(mockKardexEntity);
    }

    // ==================== registerSaleReturn() ====================
    @Test
    @DisplayName("Should register sale return successfully")
    void testRegisterSaleReturn_ValidReturn_RegistersSuccessfully() {
        // Arrange
        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(mockKardexEntity)).thenReturn(mockKardexEntity);
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        Kardex result = kardexCommandAdapter.registerSaleReturn(mockKardex);

        // Assert
        assertNotNull(result);
        verify(kardexRepository).save(mockKardexEntity);
        verify(kardexEntityCommandMapper).toDomain(mockKardexEntity);
    }

    // ==================== updateAvaliableAmount() ====================
    @Test
    @DisplayName("Should update available amount successfully")
    void testUpdateAvaliableAmount_ValidData_UpdatesSuccessfully() {
        // Arrange
        Long kardexId = 1L;
        int newAmount = 150;
        when(kardexRepository.updateAvaliableAmount(kardexId, newAmount)).thenReturn(1);

        // Act
        int result = kardexCommandAdapter.updateAvaliableAmount(kardexId, newAmount);

        // Assert
        assertEquals(1, result);
        verify(kardexRepository).updateAvaliableAmount(kardexId, newAmount);
    }

    @Test
    @DisplayName("Should return 0 when update affects no rows")
    void testUpdateAvaliableAmount_NoRowsAffected_ReturnsZero() {
        // Arrange
        Long kardexId = 999L;
        int newAmount = 100;
        when(kardexRepository.updateAvaliableAmount(kardexId, newAmount)).thenReturn(0);

        // Act
        int result = kardexCommandAdapter.updateAvaliableAmount(kardexId, newAmount);

        // Assert
        assertEquals(0, result);
        verify(kardexRepository).updateAvaliableAmount(kardexId, newAmount);
    }

    // ==================== registerNonCommercialExit() ====================
    @Test
    @DisplayName("Should register non-commercial exit with lot updates")
    void testRegisterNonCommercialExit_ValidExit_RegistersSuccessfully() {
        // Arrange
        Kardex lot = Kardex.builder().idKardex(10L).product(mockProduct).build();
        List<Kardex> lotsToUpdate = List.of(lot);

        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(50);
        detail.setUnitPrice(new BigDecimal("10.00"));
        Kardex originMovement = Kardex.builder().idKardex(10L).build();
        detail.setMovementOrigin(originMovement);
        mockKardex.setDetailsOutput(List.of(detail));

        KardexEntity lotEntity = new KardexEntity();
        KardexEntity originEntity = new KardexEntity();

        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(lot)).thenReturn(lotEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(any(KardexEntity.class))).thenReturn(mockKardexEntity);
        when(kardexRepository.getReferenceById(10L)).thenReturn(originEntity);
        when(detailOutPutRepository.save(any(DetailOutputEntity.class)))
            .thenReturn(new DetailOutputEntity());
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        Kardex result = kardexCommandAdapter.registerNonCommercialExit(mockKardex, lotsToUpdate);

        // Assert
        assertNotNull(result);
        verify(kardexRepository, times(2)).save(any(KardexEntity.class)); // 1 lot + 1 exit
        verify(detailOutPutRepository).save(any(DetailOutputEntity.class));
    }

    // ==================== registerNonCommercialEntry() ====================
    @Test
    @DisplayName("Should register non-commercial entry successfully")
    void testRegisterNonCommercialEntry_ValidEntry_RegistersSuccessfully() {
        // Arrange
        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(mockKardexEntity)).thenReturn(mockKardexEntity);
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        Kardex result = kardexCommandAdapter.registerNonCommercialEntry(mockKardex);

        // Assert
        assertNotNull(result);
        verify(productRepository).getReferenceById(1L);
        verify(kardexRepository).save(mockKardexEntity);
        verify(kardexEntityCommandMapper).toDomain(mockKardexEntity);
    }

    // ==================== Integration Scenarios ====================
    @Test
    @DisplayName("Should handle multiple detail outputs in sale")
    void testRegisterSale_MultipleDetails_SavesAllDetails() {
        // Arrange
        List<DetailOutput> details = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DetailOutput detail = new DetailOutput();
            detail.setQuantityUsed(10);
            detail.setUnitPrice(new BigDecimal("10.00"));
            Kardex origin = Kardex.builder().idKardex((long) (10 + i)).build();
            detail.setMovementOrigin(origin);
            details.add(detail);
        }
        mockKardex.setDetailsOutput(details);

        when(productRepository.getReferenceById(1L)).thenReturn(mockProductEntity);
        when(kardexEntityCommandMapper.toEntity(mockKardex)).thenReturn(mockKardexEntity);
        when(kardexRepository.save(mockKardexEntity)).thenReturn(mockKardexEntity);
        when(kardexRepository.getReferenceById(anyLong())).thenReturn(new KardexEntity());
        when(detailOutPutRepository.save(any(DetailOutputEntity.class)))
            .thenReturn(new DetailOutputEntity());
        when(kardexEntityCommandMapper.toDomain(mockKardexEntity)).thenReturn(mockKardex);

        // Act
        kardexCommandAdapter.registerSale(mockKardex, new ArrayList<>());

        // Assert
        verify(detailOutPutRepository, times(5)).save(any(DetailOutputEntity.class));
    }

}
