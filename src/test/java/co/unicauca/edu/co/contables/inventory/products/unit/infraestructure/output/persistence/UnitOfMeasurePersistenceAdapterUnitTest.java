package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.persistence;

import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.UnitOfMeasurePersistenceAdapter;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.UnitOfMeasureEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.IUnitOfMeasurePersistenceMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IUnitOfMeasureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnitOfMeasurePersistenceAdapterUnitTest {

    @Mock
    private IUnitOfMeasureRepository unitOfMeasureRepository;

    @Mock
    private IUnitOfMeasurePersistenceMapper unitOfMeasurePersistenceMapper;

    @InjectMocks
    private UnitOfMeasurePersistenceAdapter unitOfMeasurePersistenceAdapter;

    private UnitOfMeasure unitOfMeasure;
    private UnitOfMeasureEntity unitOfMeasureEntity;
    private static final Long UNIT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String UNIT_NAME = "Kilogramo";
    private static final String ABBREVIATION = "kg";

    @BeforeEach
    void setUp() {
        unitOfMeasure = UnitOfMeasure.builder()
                .id(UNIT_ID)
                .name(UNIT_NAME)
                .abbreviation(ABBREVIATION)
                .enterpriseId(ENTERPRISE_ID)
                .state(true)
                .build();

        unitOfMeasureEntity = new UnitOfMeasureEntity();
        unitOfMeasureEntity.setId(UNIT_ID);
        unitOfMeasureEntity.setName(UNIT_NAME);
        unitOfMeasureEntity.setAbbreviation(ABBREVIATION);
        unitOfMeasureEntity.setEnterpriseId(ENTERPRISE_ID);
        unitOfMeasureEntity.setState(true);
    }

    // ==================== Tests de findByIdAndEnterpriseId ====================

    @Test
    @DisplayName("Debe encontrar unidad de medida por ID y enterpriseId")
    void testFindByIdAndEnterpriseId_ReturnsUnitOfMeasure() {
        // Arrange
        when(unitOfMeasureRepository.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(unitOfMeasureEntity));
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Optional<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(UNIT_ID, result.get().getId());
        assertEquals(UNIT_NAME, result.get().getName());
        assertEquals(ABBREVIATION, result.get().getAbbreviation());
        verify(unitOfMeasureRepository).findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);
        verify(unitOfMeasurePersistenceMapper).toUnitOfMeasure(unitOfMeasureEntity);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no encuentra unidad por ID y enterpriseId")
    void testFindByIdAndEnterpriseId_ReturnsEmpty() {
        // Arrange
        when(unitOfMeasureRepository.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.empty());

        // Act
        Optional<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(unitOfMeasureRepository).findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);
        verify(unitOfMeasurePersistenceMapper, never()).toUnitOfMeasure(any());
    }

    // ==================== Tests de create ====================

    @Test
    @DisplayName("Debe crear unidad de medida correctamente")
    void testCreate_CreatesUnitOfMeasure() {
        // Arrange
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasureEntity(unitOfMeasure)).thenReturn(unitOfMeasureEntity);
        when(unitOfMeasureRepository.save(unitOfMeasureEntity)).thenReturn(unitOfMeasureEntity);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        UnitOfMeasure result = unitOfMeasurePersistenceAdapter.create(unitOfMeasure);

        // Assert
        assertNotNull(result);
        assertEquals(UNIT_ID, result.getId());
        assertEquals(UNIT_NAME, result.getName());
        assertEquals(ABBREVIATION, result.getAbbreviation());
        verify(unitOfMeasureRepository).save(unitOfMeasureEntity);
        verify(unitOfMeasurePersistenceMapper).toUnitOfMeasureEntity(unitOfMeasure);
        verify(unitOfMeasurePersistenceMapper).toUnitOfMeasure(unitOfMeasureEntity);
    }

    // ==================== Tests de deleteByIdAndEnterpriseId ====================

    @Test
    @DisplayName("Debe eliminar unidad de medida cuando existe")
    void testDeleteByIdAndEnterpriseId_DeletesWhenExists() {
        // Arrange
        when(unitOfMeasureRepository.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(unitOfMeasureEntity));
        doNothing().when(unitOfMeasureRepository).delete(unitOfMeasureEntity);

        // Act
        unitOfMeasurePersistenceAdapter.deleteByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasureRepository).findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);
        verify(unitOfMeasureRepository).delete(unitOfMeasureEntity);
    }

    @Test
    @DisplayName("No debe eliminar cuando no existe unidad de medida")
    void testDeleteByIdAndEnterpriseId_DoesNotDeleteWhenNotExists() {
        // Arrange
        when(unitOfMeasureRepository.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.empty());

        // Act
        unitOfMeasurePersistenceAdapter.deleteByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasureRepository).findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);
        verify(unitOfMeasureRepository, never()).delete(any());
    }

    // ==================== Tests de existsByNameAndEnterpriseId ====================

    @Test
    @DisplayName("Debe retornar true cuando existe unidad con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsTrue() {
        // Arrange
        when(unitOfMeasureRepository.existsByNameAndEnterpriseId(UNIT_NAME, ENTERPRISE_ID))
                .thenReturn(true);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByNameAndEnterpriseId(UNIT_NAME, ENTERPRISE_ID);

        // Assert
        assertTrue(result);
        verify(unitOfMeasureRepository).existsByNameAndEnterpriseId(UNIT_NAME, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe unidad con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsFalse() {
        // Arrange
        when(unitOfMeasureRepository.existsByNameAndEnterpriseId(UNIT_NAME, ENTERPRISE_ID))
                .thenReturn(false);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByNameAndEnterpriseId(UNIT_NAME, ENTERPRISE_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de existsByAbbreviationAndEnterpriseId ====================

    @Test
    @DisplayName("Debe retornar true cuando existe unidad con abreviación y enterpriseId")
    void testExistsByAbbreviationAndEnterpriseId_ReturnsTrue() {
        // Arrange
        when(unitOfMeasureRepository.existsByAbbreviationAndEnterpriseId(ABBREVIATION, ENTERPRISE_ID))
                .thenReturn(true);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByAbbreviationAndEnterpriseId(ABBREVIATION, ENTERPRISE_ID);

        // Assert
        assertTrue(result);
        verify(unitOfMeasureRepository).existsByAbbreviationAndEnterpriseId(ABBREVIATION, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe unidad con abreviación y enterpriseId")
    void testExistsByAbbreviationAndEnterpriseId_ReturnsFalse() {
        // Arrange
        when(unitOfMeasureRepository.existsByAbbreviationAndEnterpriseId(ABBREVIATION, ENTERPRISE_ID))
                .thenReturn(false);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByAbbreviationAndEnterpriseId(ABBREVIATION, ENTERPRISE_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de existsByNameAndEnterpriseIdAndIdNot ====================

    @Test
    @DisplayName("Debe retornar true cuando existe otra unidad con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsTrue() {
        // Arrange
        when(unitOfMeasureRepository.existsByNameAndEnterpriseIdAndIdNot(UNIT_NAME, ENTERPRISE_ID, UNIT_ID))
                .thenReturn(true);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                UNIT_NAME, ENTERPRISE_ID, UNIT_ID);

        // Assert
        assertTrue(result);
        verify(unitOfMeasureRepository).existsByNameAndEnterpriseIdAndIdNot(UNIT_NAME, ENTERPRISE_ID, UNIT_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe otra unidad con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsFalse() {
        // Arrange
        when(unitOfMeasureRepository.existsByNameAndEnterpriseIdAndIdNot(UNIT_NAME, ENTERPRISE_ID, UNIT_ID))
                .thenReturn(false);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                UNIT_NAME, ENTERPRISE_ID, UNIT_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de existsByAbbreviationAndEnterpriseIdAndIdNot ====================

    @Test
    @DisplayName("Debe retornar true cuando existe otra unidad con misma abreviación")
    void testExistsByAbbreviationAndEnterpriseIdAndIdNot_ReturnsTrue() {
        // Arrange
        when(unitOfMeasureRepository.existsByAbbreviationAndEnterpriseIdAndIdNot(ABBREVIATION, ENTERPRISE_ID, UNIT_ID))
                .thenReturn(true);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByAbbreviationAndEnterpriseIdAndIdNot(
                ABBREVIATION, ENTERPRISE_ID, UNIT_ID);

        // Assert
        assertTrue(result);
        verify(unitOfMeasureRepository).existsByAbbreviationAndEnterpriseIdAndIdNot(ABBREVIATION, ENTERPRISE_ID, UNIT_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe otra unidad con misma abreviación")
    void testExistsByAbbreviationAndEnterpriseIdAndIdNot_ReturnsFalse() {
        // Arrange
        when(unitOfMeasureRepository.existsByAbbreviationAndEnterpriseIdAndIdNot(ABBREVIATION, ENTERPRISE_ID, UNIT_ID))
                .thenReturn(false);

        // Act
        boolean result = unitOfMeasurePersistenceAdapter.existsByAbbreviationAndEnterpriseIdAndIdNot(
                ABBREVIATION, ENTERPRISE_ID, UNIT_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de getAllUnitOfMeasuresByState ====================

    @Test
    @DisplayName("Debe obtener unidades de medida por estado activo")
    void testGetAllUnitOfMeasuresByState_ActiveState_ReturnsUnits() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.getUnitOfMeasuresByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(true), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getAllUnitOfMeasuresByState(
                ENTERPRISE_ID, true, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isState());
        verify(unitOfMeasureRepository).getUnitOfMeasuresByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener unidades de medida por estado inactivo")
    void testGetAllUnitOfMeasuresByState_InactiveState_ReturnsUnits() {
        // Arrange
        unitOfMeasureEntity.setState(false);
        unitOfMeasure.setState(false);

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.getUnitOfMeasuresByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(false), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getAllUnitOfMeasuresByState(
                ENTERPRISE_ID, false, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).isState());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay unidades con estado especificado")
    void testGetAllUnitOfMeasuresByState_ReturnsEmptyPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<UnitOfMeasureEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(unitOfMeasureRepository.getUnitOfMeasuresByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(true), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getAllUnitOfMeasuresByState(
                ENTERPRISE_ID, true, pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de findByEnterpriseIdAndSearch ====================

    @Test
    @DisplayName("Debe buscar unidades de medida con filtro ascendente")
    void testFindByEnterpriseIdAndSearch_OrderAsc_ReturnsUnits() {
        // Arrange
        String search = "Kilo";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(unitOfMeasureRepository).findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe buscar unidades de medida con filtro descendente")
    void testFindByEnterpriseIdAndSearch_OrderDesc_ReturnsUnits() {
        // Arrange
        String search = "Kilo";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").descending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "name", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe buscar con ordenamiento por abreviación")
    void testFindByEnterpriseIdAndSearch_SortByAbbreviation_ReturnsUnits() {
        // Arrange
        String search = "Kilo";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("abbreviation").ascending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "abbreviation", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe usar ordenamiento por defecto cuando campo es null")
    void testFindByEnterpriseIdAndSearch_NullSortField_UsesDefault() {
        // Arrange
        String search = "Kilo";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<UnitOfMeasureEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(unitOfMeasureRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, null, "asc");

        // Assert
        assertNotNull(result);
        verify(unitOfMeasureRepository).findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe usar ordenamiento por defecto cuando campo es inválido")
    void testFindByEnterpriseIdAndSearch_InvalidSortField_UsesDefault() {
        // Arrange
        String search = "Kilo";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<UnitOfMeasureEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(unitOfMeasureRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "invalidField", "asc");

        // Assert
        assertNotNull(result);
        verify(unitOfMeasureRepository).findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando búsqueda no encuentra resultados")
    void testFindByEnterpriseIdAndSearch_ReturnsEmptyPage() {
        // Arrange
        String search = "NoExiste";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<UnitOfMeasureEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(unitOfMeasureRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de countByEnterpriseIdAndSearch ====================

    @Test
    @DisplayName("Debe contar unidades de medida con búsqueda")
    void testCountByEnterpriseIdAndSearch_ReturnsCount() {
        // Arrange
        String search = "Kilo";
        when(unitOfMeasureRepository.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search))
                .thenReturn(5L);

        // Act
        long result = unitOfMeasurePersistenceAdapter.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search);

        // Assert
        assertEquals(5L, result);
        verify(unitOfMeasureRepository).countByEnterpriseIdAndSearch(ENTERPRISE_ID, search);
    }

    @Test
    @DisplayName("Debe retornar cero cuando búsqueda no encuentra resultados")
    void testCountByEnterpriseIdAndSearch_ReturnsZero() {
        // Arrange
        String search = "NoExiste";
        when(unitOfMeasureRepository.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search))
                .thenReturn(0L);

        // Act
        long result = unitOfMeasurePersistenceAdapter.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests de getAllUnitOfMeasuresByWithSort ====================

    @Test
    @DisplayName("Debe obtener unidades de medida ordenadas ascendente por nombre")
    void testGetAllUnitOfMeasuresByWithSort_OrderByNameAsc_ReturnsUnits() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.getUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getAllUnitOfMeasuresByWithSort(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(unitOfMeasureRepository).getUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener unidades de medida ordenadas descendente por abreviación")
    void testGetAllUnitOfMeasuresByWithSort_OrderByAbbreviationDesc_ReturnsUnits() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("abbreviation").descending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.getUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getAllUnitOfMeasuresByWithSort(
                ENTERPRISE_ID, 0, 10, "abbreviation", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay unidades de medida")
    void testGetAllUnitOfMeasuresByWithSort_ReturnsEmptyPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<UnitOfMeasureEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(unitOfMeasureRepository.getUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getAllUnitOfMeasuresByWithSort(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de countByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar unidades de medida por enterpriseId")
    void testCountByEnterpriseId_ReturnsCount() {
        // Arrange
        when(unitOfMeasureRepository.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(15L);

        // Act
        long result = unitOfMeasurePersistenceAdapter.countByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(15L, result);
        verify(unitOfMeasureRepository).countByEnterpriseId(ENTERPRISE_ID);
    }

    // ==================== Tests de getActiveUnitOfMeasuresBy ====================

    @Test
    @DisplayName("Debe obtener unidades activas ordenadas ascendente")
    void testGetActiveUnitOfMeasuresBy_OrderAsc_ReturnsActiveUnits() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.getActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getActiveUnitOfMeasuresBy(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isState());
        verify(unitOfMeasureRepository).getActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener unidades activas ordenadas descendente por abreviación")
    void testGetActiveUnitOfMeasuresBy_OrderByAbbreviationDesc_ReturnsActiveUnits() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("abbreviation").descending());
        List<UnitOfMeasureEntity> entities = List.of(unitOfMeasureEntity);
        Page<UnitOfMeasureEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(unitOfMeasureRepository.getActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity)).thenReturn(unitOfMeasure);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getActiveUnitOfMeasuresBy(
                ENTERPRISE_ID, 0, 10, "abbreviation", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay unidades activas")
    void testGetActiveUnitOfMeasuresBy_ReturnsEmptyPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<UnitOfMeasureEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(unitOfMeasureRepository.getActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasurePersistenceAdapter.getActiveUnitOfMeasuresBy(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de countActiveByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar unidades activas por enterpriseId")
    void testCountActiveByEnterpriseId_ReturnsCount() {
        // Arrange
        when(unitOfMeasureRepository.countActiveByEnterpriseId(ENTERPRISE_ID)).thenReturn(10L);

        // Act
        long result = unitOfMeasurePersistenceAdapter.countActiveByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(10L, result);
        verify(unitOfMeasureRepository).countActiveByEnterpriseId(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay unidades activas")
    void testCountActiveByEnterpriseId_ReturnsZero() {
        // Arrange
        when(unitOfMeasureRepository.countActiveByEnterpriseId(ENTERPRISE_ID)).thenReturn(0L);

        // Act
        long result = unitOfMeasurePersistenceAdapter.countActiveByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests de integración ====================

    @Test
    @DisplayName("Debe invocar mapper correctamente en métodos de consulta")
    void testMapperInvocation_InQueryMethods() {
        // Arrange
        when(unitOfMeasureRepository.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(unitOfMeasureEntity));
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(any(UnitOfMeasureEntity.class))).thenReturn(unitOfMeasure);

        // Act
        unitOfMeasurePersistenceAdapter.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasurePersistenceMapper).toUnitOfMeasure(any(UnitOfMeasureEntity.class));
    }

    @Test
    @DisplayName("Debe invocar mapper correctamente en método create")
    void testMapperInvocation_InCreateMethod() {
        // Arrange
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasureEntity(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureEntity);
        when(unitOfMeasureRepository.save(any(UnitOfMeasureEntity.class))).thenReturn(unitOfMeasureEntity);
        when(unitOfMeasurePersistenceMapper.toUnitOfMeasure(any(UnitOfMeasureEntity.class))).thenReturn(unitOfMeasure);

        // Act
        unitOfMeasurePersistenceAdapter.create(unitOfMeasure);

        // Assert
        verify(unitOfMeasurePersistenceMapper).toUnitOfMeasureEntity(any(UnitOfMeasure.class));
        verify(unitOfMeasurePersistenceMapper).toUnitOfMeasure(any(UnitOfMeasureEntity.class));
    }
}
