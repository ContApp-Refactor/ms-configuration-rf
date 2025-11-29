package co.unicauca.edu.co.contables.configuration.unit.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterHasChildrenException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterInUseException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterInvalidCodePrefixException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCentersAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCentersNotFoundException;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.mapper.CostCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.mapper.CostCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.CostCenterServiceImpl;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CostCenterServiceImplUnitTest {

    @Mock
    private CostCenterRepository repository;

    @Mock
    private CostCenterDataMapper dataMapper;

    @Mock
    private CostCenterDomainMapper domainMapper;

    @InjectMocks
    private CostCenterServiceImpl service;

    private static final Long ID = 1L;
    private static final Long PARENT_ID = 2L;
    private static final String ID_ENTERPRISE = "ENT-001";
    private static final String CODE = "1001";
    private static final String NAME = "Centro de costo";
    private static final String STANDARDIZED_NAME = "Centro de costo";

    private CostCenterCreateReq createRequest;
    private CostCenterUpdateReq updateRequest;
    private CostCenterEntity entity;
    private CostCenterEntity parentEntity;
    private CostCenter domain;

    @BeforeEach
    void setUp() {
        createRequest = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        updateRequest = CostCenterUpdateReq.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        entity = CostCenterEntity.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(STANDARDIZED_NAME)
                .status(true)
                .usageCount(0)
                .build();

        parentEntity = CostCenterEntity.builder()
                .id(PARENT_ID)
                .idEnterprise(ID_ENTERPRISE)
                .code("10")
                .name("Centro padre")
                .status(true)
                .usageCount(0)
                .build();

        domain = CostCenter.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(STANDARDIZED_NAME)
                .status(true)
                .usageCount(0)
                .build();

    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("create - Debe crear centro de costo exitosamente sin padre")
    void testCreateSuccessWithoutParent() {
        // Arrange
        when(repository.existsByCodeAndIdEnterprise(CODE, ID_ENTERPRISE)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(domain);
        when(dataMapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        CostCenter result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(ID, result.getId());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("create - Debe crear centro de costo exitosamente con padre")
    void testCreateSuccessWithParent() {
        // Arrange
        createRequest.setParentId(PARENT_ID);
        when(repository.existsByCodeAndIdEnterprise(CODE, ID_ENTERPRISE)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(domain);
        when(dataMapper.toEntity(domain)).thenReturn(entity);
        when(repository.findByIdAndIdEnterprise(PARENT_ID, ID_ENTERPRISE)).thenReturn(Optional.of(parentEntity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        CostCenter result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        verify(repository).findByIdAndIdEnterprise(PARENT_ID, ID_ENTERPRISE);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("create - Debe activar padre inactivo cuando se crea hijo")
    void testCreateActivatesInactiveParent() {
        // Arrange
        createRequest.setParentId(PARENT_ID);
        parentEntity.setStatus(false);
        when(repository.existsByCodeAndIdEnterprise(CODE, ID_ENTERPRISE)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(domain);
        when(dataMapper.toEntity(domain)).thenReturn(entity);
        when(repository.findByIdAndIdEnterprise(PARENT_ID, ID_ENTERPRISE)).thenReturn(Optional.of(parentEntity));
        when(repository.save(any(CostCenterEntity.class))).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.create(createRequest);

        // Assert
        assertTrue(parentEntity.getStatus());
        verify(repository, atLeast(2)).save(any(CostCenterEntity.class));
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando código ya existe")
    void testCreateThrowsExceptionWhenCodeExists() {
        // Arrange
        when(repository.existsByCodeAndIdEnterprise(CODE, ID_ENTERPRISE)).thenReturn(true);

        // Act & Assert
        assertThrows(CostCentersAlreadyExistsException.class, () -> service.create(createRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando nombre ya existe")
    void testCreateThrowsExceptionWhenNameExists() {
        // Arrange
        when(repository.existsByCodeAndIdEnterprise(CODE, ID_ENTERPRISE)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(true);

        // Act & Assert
        assertThrows(CostCentersAlreadyExistsException.class, () -> service.create(createRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando padre no existe")
    void testCreateThrowsExceptionWhenParentNotFound() {
        // Arrange
        createRequest.setParentId(PARENT_ID);
        when(repository.existsByCodeAndIdEnterprise(CODE, ID_ENTERPRISE)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(domain);
        when(dataMapper.toEntity(domain)).thenReturn(entity);
        when(repository.findByIdAndIdEnterprise(PARENT_ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CostCentersNotFoundException.class, () -> service.create(createRequest));

        verify(repository, never()).save(any());
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("update - Debe actualizar centro de costo exitosamente")
    void testUpdateSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        CostCenter result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando centro de costo no existe")
    void testUpdateThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CostCentersNotFoundException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando centro de costo tiene movimientos")
    void testUpdateThrowsExceptionWhenInUse() {
        // Arrange
        entity.setUsageCount(5);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThrows(CostCenterInUseException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando nuevo código ya existe")
    void testUpdateThrowsExceptionWhenNewCodeExists() {
        // Arrange
        updateRequest.setCode("NUEVO");
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.existsByCodeAndIdEnterprise("NUEVO", ID_ENTERPRISE)).thenReturn(true);

        // Act & Assert
        assertThrows(CostCentersAlreadyExistsException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando nuevo nombre ya existe en otra entidad")
    void testUpdateThrowsExceptionWhenNewNameExists() {
        // Arrange
        updateRequest.setName("Nuevo nombre");
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.existsByNameAndIdEnterpriseAndIdNot(anyString(), eq(ID_ENTERPRISE), eq(ID))).thenReturn(true);

        // Act & Assert
        assertThrows(CostCentersAlreadyExistsException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando código no mantiene prefijo del padre")
    void testUpdateThrowsExceptionWhenCodePrefixInvalid() {
        // Arrange
        entity.setParent(parentEntity);
        updateRequest.setCode("2001");
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThrows(CostCenterInvalidCodePrefixException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe actualizar códigos de hijos cuando código padre cambia")
    void testUpdateCascadesCodeChangeToChildren() {
        // Arrange
        CostCenterEntity childEntity = CostCenterEntity.builder()
                .id(3L)
                .idEnterprise(ID_ENTERPRISE)
                .code("100101")
                .name("Hijo")
                .status(true)
                .usageCount(0)
                .parent(entity)
                .build();
        updateRequest.setCode("2001");
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.existsByCodeAndIdEnterprise("2001", ID_ENTERPRISE)).thenReturn(false);
        when(repository.save(entity)).thenReturn(entity);
        when(repository.findByParentId(ID)).thenReturn(List.of(childEntity));
        when(repository.save(childEntity)).thenReturn(childEntity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.update(updateRequest);

        // Assert
        assertEquals("200101", childEntity.getCode());
        verify(repository).save(childEntity);
    }

    @Test
    @DisplayName("update - Debe asignar nuevo padre correctamente")
    void testUpdateWithNewParent() {
        // Arrange
        updateRequest.setParentId(PARENT_ID);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.findByIdAndIdEnterprise(PARENT_ID, ID_ENTERPRISE)).thenReturn(Optional.of(parentEntity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.update(updateRequest);

        // Assert
        assertEquals(parentEntity, entity.getParent());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update - Debe remover padre cuando parentId es null")
    void testUpdateRemovesParentWhenNull() {
        // Arrange
        entity.setParent(parentEntity);
        updateRequest.setParentId(null);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.update(updateRequest);

        // Assert
        assertNull(entity.getParent());
        verify(repository).save(entity);
    }

    // ========== FIND ALL BY ENTERPRISE AND STATUS TESTS ==========

    @Test
    @DisplayName("findAllByEnterpriseAndStatus - Debe retornar página de centros de costo")
    void testFindAllByEnterpriseAndStatusSuccess() {
        // Arrange
        Page<CostCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterpriseAndStatus(eq(ID_ENTERPRISE), eq(true), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<CostCenter> result = service.findAllByEnterpriseAndStatus(ID_ENTERPRISE, true, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAllByIdEnterpriseAndStatus(eq(ID_ENTERPRISE), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("findAllByEnterpriseAndStatus - Debe retornar página vacía cuando no hay resultados")
    void testFindAllByEnterpriseAndStatusEmpty() {
        // Arrange
        Page<CostCenterEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(repository.findAllByIdEnterpriseAndStatus(eq(ID_ENTERPRISE), eq(true), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<CostCenter> result = service.findAllByEnterpriseAndStatus(ID_ENTERPRISE, true, 0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== FIND ALL BY ENTERPRISE HIERARCHICAL TESTS ==========

    @Test
    @DisplayName("findAllByEnterpriseHierarchical - Debe retornar jerarquía completa")
    void testFindAllByEnterpriseHierarchicalSuccess() {
        // Arrange
        CostCenterEntity childEntity = CostCenterEntity.builder()
                .id(3L)
                .idEnterprise(ID_ENTERPRISE)
                .code("1001")
                .name("Hijo")
                .status(true)
                .usageCount(0)
                .parent(entity)
                .build();
        when(repository.findByIdEnterpriseAndParentIsNullOrderByCode(ID_ENTERPRISE)).thenReturn(List.of(entity));
        when(repository.findByParentId(ID)).thenReturn(List.of(childEntity));
        when(repository.findByParentId(3L)).thenReturn(Collections.emptyList());
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        CostCenter childDomain = CostCenter.builder().id(3L).code("1001").name("Hijo").build();
        when(dataMapper.toDomain(childEntity)).thenReturn(childDomain);

        // Act
        Page<CostCenter> result = service.findAllByEnterpriseHierarchical(ID_ENTERPRISE, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    @DisplayName("findAllByEnterpriseHierarchical - Debe retornar página vacía cuando índice excede total")
    void testFindAllByEnterpriseHierarchicalEmptyWhenPageExceeds() {
        // Arrange
        when(repository.findByIdEnterpriseAndParentIsNullOrderByCode(ID_ENTERPRISE)).thenReturn(List.of(entity));
        when(repository.findByParentId(ID)).thenReturn(Collections.emptyList());

        // Act
        Page<CostCenter> result = service.findAllByEnterpriseHierarchical(ID_ENTERPRISE, 10, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("findAllByEnterpriseHierarchical - Debe retornar página vacía cuando no hay raíces")
    void testFindAllByEnterpriseHierarchicalNoRoots() {
        // Arrange
        when(repository.findByIdEnterpriseAndParentIsNullOrderByCode(ID_ENTERPRISE)).thenReturn(Collections.emptyList());

        // Act
        Page<CostCenter> result = service.findAllByEnterpriseHierarchical(ID_ENTERPRISE, 0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    @DisplayName("findById con empresa - Debe retornar centro de costo existente")
    void testFindByIdWithEnterpriseSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        CostCenter result = service.findById(ID, ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    @DisplayName("findById con empresa - Debe lanzar excepción cuando no existe")
    void testFindByIdWithEnterpriseNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CostCentersNotFoundException.class, () -> service.findById(ID, ID_ENTERPRISE));
    }

    @Test
    @DisplayName("findById sin empresa - Debe retornar centro de costo existente")
    void testFindByIdWithoutEnterpriseSuccess() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        CostCenter result = service.findById(ID);

        // Assert
        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    @DisplayName("findById sin empresa - Debe retornar null cuando no existe")
    void testFindByIdWithoutEnterpriseReturnsNull() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.empty());

        // Act
        CostCenter result = service.findById(ID);

        // Assert
        assertNull(result);
    }

    // ========== CHANGE STATE TESTS ==========

    @Test
    @DisplayName("changeState - Debe activar centro de costo")
    void testChangeStateActivate() {
        // Arrange
        entity.setStatus(false);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        CostCenter result = service.changeState(ID, ID_ENTERPRISE, true);

        // Assert
        assertNotNull(result);
        assertTrue(entity.getStatus());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("changeState - Debe desactivar centro de costo y sus hijos")
    void testChangeStateDeactivateWithChildren() {
        // Arrange
        CostCenterEntity childEntity = CostCenterEntity.builder()
                .id(3L)
                .idEnterprise(ID_ENTERPRISE)
                .code("1001")
                .name("Hijo")
                .status(true)
                .usageCount(0)
                .parent(entity)
                .build();
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(any(CostCenterEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findByParentId(ID)).thenReturn(List.of(childEntity));
        when(repository.findByParentId(3L)).thenReturn(Collections.emptyList());
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.changeState(ID, ID_ENTERPRISE, false);

        // Assert
        assertFalse(entity.getStatus());
        assertFalse(childEntity.getStatus());
        verify(repository, times(2)).save(any(CostCenterEntity.class));
    }

    @Test
    @DisplayName("changeState - Debe activar padre cuando se activa hijo")
    void testChangeStateActivatesParent() {
        // Arrange
        entity.setParent(parentEntity);
        parentEntity.setStatus(false);
        entity.setStatus(false);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(any(CostCenterEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.changeState(ID, ID_ENTERPRISE, true);

        // Assert
        assertTrue(parentEntity.getStatus());
        verify(repository, atLeast(2)).save(any(CostCenterEntity.class));
    }

    @Test
    @DisplayName("changeState - Debe lanzar excepción cuando no existe")
    void testChangeStateThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CostCentersNotFoundException.class, () -> service.changeState(ID, ID_ENTERPRISE, true));

        verify(repository, never()).save(any());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("delete - Debe eliminar centro de costo exitosamente")
    void testDeleteSuccess() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.existsByParentId(ID)).thenReturn(false);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        CostCenter result = service.delete(ID, ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando no existe")
    void testDeleteThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CostCentersNotFoundException.class, () -> service.delete(ID, ID_ENTERPRISE));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando tiene movimientos")
    void testDeleteThrowsExceptionWhenInUse() {
        // Arrange
        entity.setUsageCount(5);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThrows(CostCenterInUseException.class, () -> service.delete(ID, ID_ENTERPRISE));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando tiene hijos")
    void testDeleteThrowsExceptionWhenHasChildren() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.existsByParentId(ID)).thenReturn(true);

        // Act & Assert
        assertThrows(CostCenterHasChildrenException.class, () -> service.delete(ID, ID_ENTERPRISE));

        verify(repository, never()).delete(any());
    }

    // ========== FIND ACTIVE LAST LEVEL COST CENTERS TESTS ==========

    @Test
    @DisplayName("findActiveLastLevelCostCenters - Debe retornar centros de costo auxiliares")
    void testFindActiveLastLevelCostCentersSuccess() {
        // Arrange
        when(repository.findAuxiliaryCostCenters(ID_ENTERPRISE)).thenReturn(List.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        List<CostCenter> result = service.findActiveLastLevelCostCenters(ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findActiveLastLevelCostCenters - Debe retornar lista vacía cuando no hay resultados")
    void testFindActiveLastLevelCostCentersEmpty() {
        // Arrange
        when(repository.findAuxiliaryCostCenters(ID_ENTERPRISE)).thenReturn(Collections.emptyList());

        // Act
        List<CostCenter> result = service.findActiveLastLevelCostCenters(ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== COUNT TESTS ==========

    @Test
    @DisplayName("countAllByEnterprise - Debe retornar conteo correcto")
    void testCountAllByEnterpriseSuccess() {
        // Arrange
        when(repository.countByIdEnterprise(ID_ENTERPRISE)).thenReturn(10L);

        // Act
        long result = service.countAllByEnterprise(ID_ENTERPRISE);

        // Assert
        assertEquals(10L, result);
    }

    @Test
    @DisplayName("countAllByEnterpriseAndStatus - Debe retornar conteo correcto")
    void testCountAllByEnterpriseAndStatusSuccess() {
        // Arrange
        when(repository.countByIdEnterpriseAndStatus(ID_ENTERPRISE, true)).thenReturn(5L);

        // Act
        long result = service.countAllByEnterpriseAndStatus(ID_ENTERPRISE, true);

        // Assert
        assertEquals(5L, result);
    }

    // ========== FIND BY ENTERPRISE AND SEARCH TESTS ==========

    @Test
    @DisplayName("findByEnterpriseAndSearch - Debe retornar página de resultados")
    void testFindByEnterpriseAndSearchSuccess() {
        // Arrange
        String search = "centro";
        Page<CostCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
                eq(ID_ENTERPRISE), eq(search), eq(ID_ENTERPRISE), eq(search), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<CostCenter> result = service.findByEnterpriseAndSearch(ID_ENTERPRISE, search, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("findByEnterpriseAndSearch - Debe retornar página vacía sin resultados")
    void testFindByEnterpriseAndSearchEmpty() {
        // Arrange
        String search = "noexiste";
        Page<CostCenterEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(repository.findByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
                eq(ID_ENTERPRISE), eq(search), eq(ID_ENTERPRISE), eq(search), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<CostCenter> result = service.findByEnterpriseAndSearch(ID_ENTERPRISE, search, 0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("countByEnterpriseAndSearch - Debe retornar conteo correcto")
    void testCountByEnterpriseAndSearchSuccess() {
        // Arrange
        String search = "centro";
        when(repository.countByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
                ID_ENTERPRISE, search, ID_ENTERPRISE, search)).thenReturn(3L);

        // Act
        long result = service.countByEnterpriseAndSearch(ID_ENTERPRISE, search);

        // Assert
        assertEquals(3L, result);
    }

    // ========== UPDATE USAGE COUNT TESTS ==========

    @Test
    @DisplayName("updateUsageCount - Debe actualizar contador de uso exitosamente")
    void testUpdateUsageCountSuccess() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        // Act
        service.updateUsageCount(ID, 10);

        // Assert
        assertEquals(10, entity.getUsageCount());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("updateUsageCount - Debe lanzar excepción cuando no existe")
    void testUpdateUsageCountThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CostCentersNotFoundException.class, () -> service.updateUsageCount(ID, 10));

        verify(repository, never()).save(any());
    }

    // ========== HIERARCHICAL PARENT ACTIVATION TESTS ==========

    @Test
    @DisplayName("create - Debe activar jerarquía completa de padres inactivos")
    void testCreateActivatesEntireParentHierarchy() {
        // Arrange
        CostCenterEntity grandParentEntity = CostCenterEntity.builder()
                .id(4L)
                .idEnterprise(ID_ENTERPRISE)
                .code("1")
                .name("Abuelo")
                .status(false)
                .usageCount(0)
                .build();
        parentEntity.setStatus(false);
        parentEntity.setParent(grandParentEntity);
        createRequest.setParentId(PARENT_ID);

        when(repository.existsByCodeAndIdEnterprise(CODE, ID_ENTERPRISE)).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(domain);
        when(dataMapper.toEntity(domain)).thenReturn(entity);
        when(repository.findByIdAndIdEnterprise(PARENT_ID, ID_ENTERPRISE)).thenReturn(Optional.of(parentEntity));
        when(repository.save(any(CostCenterEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.create(createRequest);

        // Assert
        assertTrue(parentEntity.getStatus());
        assertTrue(grandParentEntity.getStatus());
    }

    // ========== UPDATE WITH ENTERPRISE CHANGE TESTS ==========

    @Test
    @DisplayName("update - Debe validar código cuando cambia empresa")
    void testUpdateValidatesCodeWhenEnterpriseChanges() {
        // Arrange
        String newEnterprise = "ENT-002";
        updateRequest.setIdEnterprise(newEnterprise);
        when(repository.findByIdAndIdEnterprise(ID, newEnterprise)).thenReturn(Optional.of(entity));
        when(repository.existsByCodeAndIdEnterprise(CODE, newEnterprise)).thenReturn(true);

        // Act & Assert
        assertThrows(CostCentersAlreadyExistsException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe validar nombre cuando cambia empresa")
    void testUpdateValidatesNameWhenEnterpriseChanges() {
        // Arrange
        String newEnterprise = "ENT-002";
        updateRequest.setIdEnterprise(newEnterprise);
        when(repository.findByIdAndIdEnterprise(ID, newEnterprise)).thenReturn(Optional.of(entity));
        when(repository.existsByCodeAndIdEnterprise(CODE, newEnterprise)).thenReturn(false);
        when(repository.existsByNameAndIdEnterpriseAndIdNot(anyString(), eq(newEnterprise), eq(ID))).thenReturn(true);

        // Act & Assert
        assertThrows(CostCentersAlreadyExistsException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    // ========== RECURSIVE CHILDREN CODE UPDATE TESTS ==========

    @Test
    @DisplayName("update - Debe actualizar códigos de nietos cuando código abuelo cambia")
    void testUpdateCascadesCodeChangeToGrandchildren() {
        // Arrange
        CostCenterEntity childEntity = CostCenterEntity.builder()
                .id(3L)
                .idEnterprise(ID_ENTERPRISE)
                .code("100101")
                .name("Hijo")
                .status(true)
                .usageCount(0)
                .parent(entity)
                .build();
        CostCenterEntity grandchildEntity = CostCenterEntity.builder()
                .id(4L)
                .idEnterprise(ID_ENTERPRISE)
                .code("10010101")
                .name("Nieto")
                .status(true)
                .usageCount(0)
                .parent(childEntity)
                .build();
        updateRequest.setCode("2001");
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.existsByCodeAndIdEnterprise("2001", ID_ENTERPRISE)).thenReturn(false);
        when(repository.save(any(CostCenterEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findByParentId(ID)).thenReturn(List.of(childEntity));
        when(repository.findByParentId(3L)).thenReturn(List.of(grandchildEntity));
        when(repository.findByParentId(4L)).thenReturn(Collections.emptyList());
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.update(updateRequest);

        // Assert
        assertEquals("200101", childEntity.getCode());
        assertEquals("20010101", grandchildEntity.getCode());
    }

    // ========== CHANGE STATE RECURSIVE TESTS ==========

    @Test
    @DisplayName("changeState - Debe desactivar nietos recursivamente")
    void testChangeStateDeactivatesGrandchildrenRecursively() {
        // Arrange
        CostCenterEntity childEntity = CostCenterEntity.builder()
                .id(3L)
                .idEnterprise(ID_ENTERPRISE)
                .code("1001")
                .name("Hijo")
                .status(true)
                .usageCount(0)
                .parent(entity)
                .build();
        CostCenterEntity grandchildEntity = CostCenterEntity.builder()
                .id(4L)
                .idEnterprise(ID_ENTERPRISE)
                .code("100101")
                .name("Nieto")
                .status(true)
                .usageCount(0)
                .parent(childEntity)
                .build();
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(any(CostCenterEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findByParentId(ID)).thenReturn(List.of(childEntity));
        when(repository.findByParentId(3L)).thenReturn(List.of(grandchildEntity));
        when(repository.findByParentId(4L)).thenReturn(Collections.emptyList());
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.changeState(ID, ID_ENTERPRISE, false);

        // Assert
        assertFalse(entity.getStatus());
        assertFalse(childEntity.getStatus());
        assertFalse(grandchildEntity.getStatus());
    }
}
