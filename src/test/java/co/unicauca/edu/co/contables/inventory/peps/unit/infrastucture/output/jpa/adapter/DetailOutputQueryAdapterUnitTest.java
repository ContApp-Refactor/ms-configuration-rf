package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.DetailOutputQueryAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IDetailOutPutSaleReturnMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IDetailOutPutRepository;

@ExtendWith(MockitoExtension.class)
public class DetailOutputQueryAdapterUnitTest {
    
    @Mock
    private IDetailOutPutRepository detailOutPutRepository;
    
    @Mock
    private IDetailOutPutSaleReturnMapper detailOutPutSaleReturnMapper;

    @InjectMocks
    private DetailOutputQueryAdapter detailOutputQueryAdapter;

    private DetailOutputEntity mockEntity;
    private DetailOutput mockDomain;
    private List<DetailOutputEntity> mockEntities;
    private List<DetailOutput> mockDomainList;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        mockEntity = createMockEntity(1L);
        mockDomain = createMockDomain(1L);
        mockEntities = createMockEntities();
        mockDomainList = createMockDomainList();
    }

    // ==================== findByMovementSale() ====================
    @Test
    @DisplayName("Should find detail outputs by movement sale ID")
    void testFindByMovementSale_ValidId_ReturnsDetailList() {
        // Arrange
        Long movementSaleId = 10L;
        when(detailOutPutRepository.findByMovementSaleId(movementSaleId))
            .thenReturn(mockEntities);
        when(detailOutPutSaleReturnMapper.toDomainList(mockEntities))
            .thenReturn(mockDomainList);

        // Act
        List<DetailOutput> result = detailOutputQueryAdapter.findByMovementSale(movementSaleId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(detailOutPutRepository).findByMovementSaleId(movementSaleId);
        verify(detailOutPutSaleReturnMapper).toDomainList(mockEntities);
    }

    @Test
    @DisplayName("Should return empty list when no details found")
    void testFindByMovementSale_NoDetailsFound_ReturnsEmptyList() {
        // Arrange
        Long movementSaleId = 999L;
        when(detailOutPutRepository.findByMovementSaleId(movementSaleId))
            .thenReturn(Collections.emptyList());
        when(detailOutPutSaleReturnMapper.toDomainList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // Act
        List<DetailOutput> result = detailOutputQueryAdapter.findByMovementSale(movementSaleId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(detailOutPutRepository).findByMovementSaleId(movementSaleId);
    }


    @Test
    @DisplayName("Should delete detail output by ID")
    void testDeleteById_ValidId_DeletesSuccessfully() {
        // Arrange
        Long detailId = 1L;
        doNothing().when(detailOutPutRepository).deleteById(detailId);

        // Act
        detailOutputQueryAdapter.deleteById(detailId);

        // Assert
        verify(detailOutPutRepository).deleteById(detailId);
    }

    @Test
    @DisplayName("Should handle delete when ID does not exist")
    void testDeleteById_NonExistentId_DoesNotThrowException() {
        // Arrange
        Long nonExistentId = 999L;
        doNothing().when(detailOutPutRepository).deleteById(nonExistentId);

        // Act & Assert
        assertDoesNotThrow(() -> detailOutputQueryAdapter.deleteById(nonExistentId));
        verify(detailOutPutRepository).deleteById(nonExistentId);
    }

   
    @Test
    @DisplayName("Should find details by movement sale ordered descending")
    void testFindByMovementSaleOrderedDesc_ValidId_ReturnsOrderedList() {
        // Arrange
        Long kardexSaleId = 10L;
        when(detailOutPutRepository.findByMovementSaleOrderedDesc(kardexSaleId))
            .thenReturn(mockEntities);
        when(detailOutPutSaleReturnMapper.toDomainList(mockEntities))
            .thenReturn(mockDomainList);

        // Act
        List<DetailOutput> result = detailOutputQueryAdapter.findByMovementSaleOrderedDesc(kardexSaleId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(detailOutPutRepository).findByMovementSaleOrderedDesc(kardexSaleId);
        verify(detailOutPutSaleReturnMapper).toDomainList(mockEntities);
    }

    @Test
    @DisplayName("Should return empty list when no details found ordered desc")
    void testFindByMovementSaleOrderedDesc_NoDetails_ReturnsEmptyList() {
        // Arrange
        Long kardexSaleId = 999L;
        when(detailOutPutRepository.findByMovementSaleOrderedDesc(kardexSaleId))
            .thenReturn(Collections.emptyList());
        when(detailOutPutSaleReturnMapper.toDomainList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // Act
        List<DetailOutput> result = detailOutputQueryAdapter.findByMovementSaleOrderedDesc(kardexSaleId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(detailOutPutRepository).findByMovementSaleOrderedDesc(kardexSaleId);
    }

    @Test
    @DisplayName("Should return details in LIFO order")
    void testFindByMovementSaleOrderedDesc_ValidData_ReturnsLIFOOrder() {
        // Arrange
        Long kardexSaleId = 10L;
        List<DetailOutputEntity> orderedEntities = Arrays.asList(
            createMockEntity(3L),
            createMockEntity(2L),
            createMockEntity(1L)
        );
        List<DetailOutput> orderedDomain = Arrays.asList(
            createMockDomain(3L),
            createMockDomain(2L),
            createMockDomain(1L)
        );
        
        when(detailOutPutRepository.findByMovementSaleOrderedDesc(kardexSaleId))
            .thenReturn(orderedEntities);
        when(detailOutPutSaleReturnMapper.toDomainList(orderedEntities))
            .thenReturn(orderedDomain);

        // Act
        List<DetailOutput> result = detailOutputQueryAdapter.findByMovementSaleOrderedDesc(kardexSaleId);

        // Assert
        assertEquals(3, result.size());
        assertEquals(3L, result.get(0).getIdDetailOutput());
        assertEquals(2L, result.get(1).getIdDetailOutput());
        assertEquals(1L, result.get(2).getIdDetailOutput());
    }

    // ==================== update() ====================
    @Test
    @DisplayName("Should update detail output successfully")
    void testUpdate_ValidDetail_ReturnsUpdatedDetail() {
        // Arrange
        when(detailOutPutSaleReturnMapper.toEntity(mockDomain))
            .thenReturn(mockEntity);
        when(detailOutPutRepository.save(mockEntity))
            .thenReturn(mockEntity);
        when(detailOutPutSaleReturnMapper.toDomain(mockEntity))
            .thenReturn(mockDomain);

        // Act
        DetailOutput result = detailOutputQueryAdapter.update(mockDomain);

        // Assert
        assertNotNull(result);
        assertEquals(mockDomain.getIdDetailOutput(), result.getIdDetailOutput());
        verify(detailOutPutSaleReturnMapper).toEntity(mockDomain);
        verify(detailOutPutRepository).save(mockEntity);
        verify(detailOutPutSaleReturnMapper).toDomain(mockEntity);
    }

    @Test
    @DisplayName("Should update detail with modified quantity")
    void testUpdate_ModifiedQuantity_SavesChanges() {
        // Arrange
        DetailOutput modifiedDetail = createMockDomain(1L);
        modifiedDetail.setQuantityUsed(50); // Modified quantity
        
        DetailOutputEntity modifiedEntity = createMockEntity(1L);
        
        when(detailOutPutSaleReturnMapper.toEntity(modifiedDetail))
            .thenReturn(modifiedEntity);
        when(detailOutPutRepository.save(modifiedEntity))
            .thenReturn(modifiedEntity);
        when(detailOutPutSaleReturnMapper.toDomain(modifiedEntity))
            .thenReturn(modifiedDetail);

        // Act
        DetailOutput result = detailOutputQueryAdapter.update(modifiedDetail);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getQuantityUsed());
        verify(detailOutPutRepository).save(any(DetailOutputEntity.class));
    }

    @Test
    @DisplayName("Should update detail with modified price")
    void testUpdate_ModifiedPrice_SavesChanges() {
        // Arrange
        DetailOutput modifiedDetail = createMockDomain(1L);
        modifiedDetail.setUnitPrice(new BigDecimal("99.99"));
        
        DetailOutputEntity modifiedEntity = createMockEntity(1L);
        
        when(detailOutPutSaleReturnMapper.toEntity(modifiedDetail))
            .thenReturn(modifiedEntity);
        when(detailOutPutRepository.save(modifiedEntity))
            .thenReturn(modifiedEntity);
        when(detailOutPutSaleReturnMapper.toDomain(modifiedEntity))
            .thenReturn(modifiedDetail);

        // Act
        DetailOutput result = detailOutputQueryAdapter.update(modifiedDetail);

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal("99.99").compareTo(result.getUnitPrice()));
        verify(detailOutPutRepository).save(any(DetailOutputEntity.class));
    }

    // ==================== Integration Tests ====================
    @Test
    @DisplayName("Should handle full workflow: find, update, verify")
    void testFullWorkflow_FindUpdateVerify_WorksCorrectly() {
        // Arrange
        Long movementSaleId = 10L;
        when(detailOutPutRepository.findByMovementSaleId(movementSaleId))
            .thenReturn(List.of(mockEntity));
        when(detailOutPutSaleReturnMapper.toDomainList(anyList()))
            .thenReturn(List.of(mockDomain));
        when(detailOutPutSaleReturnMapper.toEntity(mockDomain))
            .thenReturn(mockEntity);
        when(detailOutPutRepository.save(mockEntity))
            .thenReturn(mockEntity);
        when(detailOutPutSaleReturnMapper.toDomain(mockEntity))
            .thenReturn(mockDomain);

        // Act
        List<DetailOutput> found = detailOutputQueryAdapter.findByMovementSale(movementSaleId);
        DetailOutput toUpdate = found.get(0);
        DetailOutput updated = detailOutputQueryAdapter.update(toUpdate);

        // Assert
        assertNotNull(found);
        assertNotNull(updated);
        assertEquals(1, found.size());
        verify(detailOutPutRepository).findByMovementSaleId(movementSaleId);
        verify(detailOutPutRepository).save(mockEntity);
    }

   
    private DetailOutputEntity createMockEntity(Long id) {
        DetailOutputEntity entity = new DetailOutputEntity();
        entity.setIdDetailOutput(id);
        entity.setQuantityUsed(100);
        entity.setUnitPrice(new BigDecimal("10.50"));
        return entity;
    }

    private DetailOutput createMockDomain(Long id) {
        DetailOutput domain = new DetailOutput();
        domain.setIdDetailOutput(id);
        domain.setQuantityUsed(100);
        domain.setUnitPrice(new BigDecimal("10.50"));
        return domain;
    }

    private List<DetailOutputEntity> createMockEntities() {
        List<DetailOutputEntity> entities = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            entities.add(createMockEntity(i));
        }
        return entities;
    }

    private List<DetailOutput> createMockDomainList() {
        List<DetailOutput> domains = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            domains.add(createMockDomain(i));
        }
        return domains;
    }
}
