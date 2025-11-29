package co.unicauca.edu.co.contables.configuration.unit.helpCenter.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.HelpCenterAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.HelpCenterNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.InvalidModuleException;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.mapper.HelpCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.repository.HelpCenterRepository;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.mapper.HelpCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.services.HelpCenterServiceImpl;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
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
class HelpCenterServiceImplUnitTest {

    @Mock
    private HelpCenterRepository repository;

    @Mock
    private HelpCenterDataMapper dataMapper;

    @Mock
    private HelpCenterDomainMapper domainMapper;

    @InjectMocks
    private HelpCenterServiceImpl service;

    private static final Long ID = 1L;
    private static final Integer MODULE_ID = 1;
    private static final String NAME = "ayuda inventario";
    private static final String STANDARDIZED_NAME = "Ayuda inventario";
    private static final String DESCRIPTION = "Descripción de ayuda";

    private HelpCenter domain;
    private HelpCenterEntity entity;
    private HelpCenterCreateReq createRequest;
    private HelpCenterUpdateReq updateRequest;

    @BeforeEach
    void setUp() {
        domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(STANDARDIZED_NAME)
                .description(DESCRIPTION)
                .status(true)
                .build();

        entity = HelpCenterEntity.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .module(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO)
                .name(STANDARDIZED_NAME)
                .description(DESCRIPTION)
                .status(true)
                .build();

        createRequest = HelpCenterCreateReq.builder()
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();

        updateRequest = HelpCenterUpdateReq.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("create - Debe crear registro de ayuda exitosamente")
    void testCreateSuccess() {
        // Arrange
        when(repository.existsByName(anyString())).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(domain);
        when(dataMapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        HelpCenter result = service.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(ID, result.getId());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando módulo es inválido")
    void testCreateThrowsExceptionWhenInvalidModule() {
        // Arrange
        createRequest.setModuleId(999);

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> service.create(createRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando nombre ya existe")
    void testCreateThrowsExceptionWhenNameExists() {
        // Arrange
        when(repository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(HelpCenterAlreadyExistsException.class, () -> service.create(createRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe estandarizar nombre antes de guardar")
    void testCreateStandardizesName() {
        // Arrange
        createRequest.setName("  AYUDA   INVENTARIO  ");
        when(repository.existsByName(anyString())).thenReturn(false);
        when(domainMapper.toDomain(createRequest)).thenReturn(domain);
        when(dataMapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.create(createRequest);

        // Assert
        verify(repository).existsByName(anyString());
        verify(repository).save(entity);
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("update - Debe actualizar registro de ayuda exitosamente")
    void testUpdateSuccess() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        HelpCenter result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando módulo es inválido")
    void testUpdateThrowsExceptionWhenInvalidModule() {
        // Arrange
        updateRequest.setModuleId(999);

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando registro no existe")
    void testUpdateThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(HelpCenterNotFoundException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando nuevo nombre ya existe en otro registro")
    void testUpdateThrowsExceptionWhenNewNameExists() {
        // Arrange
        updateRequest.setName("Otro nombre");
        entity.setName("Nombre actual");
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(repository.existsByNameAndIdNot(anyString(), eq(ID))).thenReturn(true);

        // Act & Assert
        assertThrows(HelpCenterAlreadyExistsException.class, () -> service.update(updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe permitir actualizar cuando nombre no cambia")
    void testUpdateAllowsWhenNameNotChanged() {
        // Arrange
        entity.setName(STANDARDIZED_NAME);
        updateRequest.setName(NAME);
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        HelpCenter result = service.update(updateRequest);

        // Assert
        assertNotNull(result);
        verify(repository, never()).existsByNameAndIdNot(anyString(), anyLong());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update - Debe actualizar módulo correctamente")
    void testUpdateChangesModule() {
        // Arrange
        updateRequest.setModuleId(2);
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        service.update(updateRequest);

        // Assert
        assertEquals(DocumentModule.INVENTARIO_PEPS, entity.getModule());
        assertEquals(2, entity.getModuleId());
        verify(repository).save(entity);
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    @DisplayName("findById - Debe retornar registro existente")
    void testFindByIdSuccess() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        HelpCenter result = service.findById(ID);

        // Assert
        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando no existe")
    void testFindByIdThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(HelpCenterNotFoundException.class, () -> service.findById(ID));
    }

    // ========== FIND ALL TESTS ==========

    @Test
    @DisplayName("findAll - Debe retornar página de registros")
    void testFindAllSuccess() {
        // Arrange
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("findAll - Debe retornar página vacía cuando no hay registros")
    void testFindAllEmpty() {
        // Arrange
        Page<HelpCenterEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll con ordenamiento - Debe ordenar ascendente por defecto")
    void testFindAllWithSortAscending() {
        // Arrange
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("findAll con ordenamiento - Debe ordenar descendente")
    void testFindAllWithSortDescending() {
        // Arrange
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10, "name", "desc");

        // Assert
        assertNotNull(result);
        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("findAll con ordenamiento - Debe usar campo por defecto cuando sortField es null")
    void testFindAllWithNullSortField() {
        // Arrange
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10, null, "asc");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("findAll con ordenamiento - Debe usar campo por defecto cuando sortField es inválido")
    void testFindAllWithInvalidSortField() {
        // Arrange
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10, "invalid_field", "asc");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("findAll con ordenamiento - Debe ordenar por module")
    void testFindAllWithSortByModule() {
        // Arrange
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10, "module", "asc");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("findAll con ordenamiento - Debe ordenar por moduleId")
    void testFindAllWithSortByModuleId() {
        // Arrange
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findAll(0, 10, "moduleId", "asc");

        // Assert
        assertNotNull(result);
    }

    // ========== FIND ALL BY MODULE TESTS ==========

    @Test
    @DisplayName("findAllByModule - Debe retornar lista de registros por módulo")
    void testFindAllByModuleSuccess() {
        // Arrange
        when(repository.findAllByModuleAndStatus(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, true))
                .thenReturn(List.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        List<HelpCenter> result = service.findAllByModule(MODULE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findAllByModule - Debe lanzar excepción cuando módulo es inválido")
    void testFindAllByModuleThrowsExceptionWhenInvalidModule() {
        // Arrange
        Integer invalidModuleId = 999;

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> service.findAllByModule(invalidModuleId));
    }

    @Test
    @DisplayName("findAllByModule - Debe retornar lista vacía cuando no hay registros")
    void testFindAllByModuleEmpty() {
        // Arrange
        when(repository.findAllByModuleAndStatus(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, true))
                .thenReturn(Collections.emptyList());

        // Act
        List<HelpCenter> result = service.findAllByModule(MODULE_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== CHANGE STATE TESTS ==========

    @Test
    @DisplayName("changeState - Debe activar registro")
    void testChangeStateActivate() {
        // Arrange
        entity.setStatus(false);
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        HelpCenter result = service.changeState(ID, true);

        // Assert
        assertNotNull(result);
        assertTrue(entity.getStatus());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("changeState - Debe desactivar registro")
    void testChangeStateDeactivate() {
        // Arrange
        entity.setStatus(true);
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        domain.setStatus(false);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        HelpCenter result = service.changeState(ID, false);

        // Assert
        assertNotNull(result);
        assertFalse(entity.getStatus());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("changeState - Debe lanzar excepción cuando registro no existe")
    void testChangeStateThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(HelpCenterNotFoundException.class, () -> service.changeState(ID, true));

        verify(repository, never()).save(any());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("delete - Debe eliminar registro exitosamente")
    void testDeleteSuccess() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        HelpCenter result = service.delete(ID);

        // Assert
        assertNotNull(result);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando registro no existe")
    void testDeleteThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findById(ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(HelpCenterNotFoundException.class, () -> service.delete(ID));

        verify(repository, never()).delete(any());
    }

    // ========== COUNT TESTS ==========

    @Test
    @DisplayName("countAll - Debe retornar conteo correcto")
    void testCountAllSuccess() {
        // Arrange
        when(repository.count()).thenReturn(10L);

        // Act
        long result = service.countAll();

        // Assert
        assertEquals(10L, result);
    }

    @Test
    @DisplayName("countAll - Debe retornar cero cuando no hay registros")
    void testCountAllZero() {
        // Arrange
        when(repository.count()).thenReturn(0L);

        // Act
        long result = service.countAll();

        // Assert
        assertEquals(0L, result);
    }

    // ========== FIND BY NAME CONTAINING TESTS ==========

    @Test
    @DisplayName("findByNameContaining - Debe retornar página de resultados")
    void testFindByNameContainingSuccess() {
        // Arrange
        String search = "ayuda";
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.searchByText(eq(search), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findByNameContaining(search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("findByNameContaining - Debe retornar página vacía sin resultados")
    void testFindByNameContainingEmpty() {
        // Arrange
        String search = "noexiste";
        Page<HelpCenterEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(repository.searchByText(eq(search), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<HelpCenter> result = service.findByNameContaining(search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByNameContaining - Debe ordenar descendente")
    void testFindByNameContainingDescending() {
        // Arrange
        String search = "ayuda";
        Page<HelpCenterEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.searchByText(eq(search), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<HelpCenter> result = service.findByNameContaining(search, 0, 10, "name", "desc");

        // Assert
        assertNotNull(result);
    }

    // ========== COUNT BY NAME CONTAINING TESTS ==========

    @Test
    @DisplayName("countByNameContaining - Debe retornar conteo correcto")
    void testCountByNameContainingSuccess() {
        // Arrange
        String search = "ayuda";
        when(repository.countByText(search)).thenReturn(5L);

        // Act
        long result = service.countByNameContaining(search);

        // Assert
        assertEquals(5L, result);
    }

    @Test
    @DisplayName("countByNameContaining - Debe retornar cero sin resultados")
    void testCountByNameContainingZero() {
        // Arrange
        String search = "noexiste";
        when(repository.countByText(search)).thenReturn(0L);

        // Act
        long result = service.countByNameContaining(search);

        // Assert
        assertEquals(0L, result);
    }
}
