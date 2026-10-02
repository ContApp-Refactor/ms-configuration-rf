package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller.UnitMeasureRestController;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.UnitOfMeasureCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.UnitOfMeasureResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.IUnitOfMeasureRestMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnitMeasureRestControllerUnitTest {

    @Mock
    private IUnitOfMeasureServicePort unitOfMeasureServicePort;

    @Mock
    private IUnitOfMeasureRestMapper unitOfMeasureRestMapper;

    @InjectMocks
    private UnitMeasureRestController unitMeasureRestController;

    private UnitOfMeasure unitOfMeasure;
    private UnitOfMeasureCreateRequest unitOfMeasureCreateRequest;
    private UnitOfMeasureResponse unitOfMeasureResponse;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final Long UNIT_ID = 1L;

    @BeforeEach
    void setUp() {
        unitOfMeasure = UnitOfMeasure.builder()
                .id(UNIT_ID)
                .enterpriseId(ENTERPRISE_ID)
                .name("Kilogramo")
                .abbreviation("kg")
                .state(true)
                .build();

        unitOfMeasureCreateRequest = UnitOfMeasureCreateRequest.builder()
                .enterpriseId(ENTERPRISE_ID)
                .name("Kilogramo")
                .abbreviation("kg")
                .build();

        unitOfMeasureResponse = new UnitOfMeasureResponse();
        unitOfMeasureResponse.setId(UNIT_ID);
        unitOfMeasureResponse.setEnterpriseId(ENTERPRISE_ID);
        unitOfMeasureResponse.setName("Kilogramo");
        unitOfMeasureResponse.setAbbreviation("kg");
        unitOfMeasureResponse.setState(true);
    }

    // ==================== Tests de findAll sin búsqueda ====================

    @Test
    @DisplayName("Debe obtener lista paginada de unidades de medida sin búsqueda")
    void testFindAll_WithoutSearch_ReturnsPaginatedList() {
        // Arrange
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countAllUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(unitOfMeasureServicePort.getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findAll(
                ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(unitOfMeasureServicePort).countAllUnitOfMeasuresByEntId(ENTERPRISE_ID);
        verify(unitOfMeasureServicePort).getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"));
        verify(unitOfMeasureServicePort, never()).countByEntIdAndSearch(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento personalizado en findAll")
    void testFindAll_AppliesCustomSorting() {
        // Arrange
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countAllUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(unitOfMeasureServicePort.getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("abbreviation"), eq("desc"))).thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.findAll(ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "abbreviation", "desc");

        // Assert
        verify(unitOfMeasureServicePort).getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("abbreviation"), eq("desc"));
    }

    @Test
    @DisplayName("Debe usar parámetros por defecto en findAll")
    void testFindAll_WithoutParams_UsesDefaults() {
        // Arrange
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countAllUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(unitOfMeasureServicePort.getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findAll(
                ENTERPRISE_ID, null, Optional.empty(), Optional.empty(), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    // ==================== Tests de findAll con búsqueda ====================

    @Test
    @DisplayName("Debe buscar unidades de medida con término de búsqueda")
    void testFindAll_WithSearch_ReturnsFilteredList() {
        // Arrange
        String searchTerm = "Kilo";
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countByEntIdAndSearch(ENTERPRISE_ID, searchTerm)).thenReturn(1L);
        when(unitOfMeasureServicePort.findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), anyInt(), 
                anyInt(), eq("name"), eq("asc"))).thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findAll(
                ENTERPRISE_ID, searchTerm, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(unitOfMeasureServicePort).countByEntIdAndSearch(ENTERPRISE_ID, searchTerm);
        verify(unitOfMeasureServicePort).findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), 
                anyInt(), anyInt(), eq("name"), eq("asc"));
        verify(unitOfMeasureServicePort, never()).countAllUnitOfMeasuresByEntId(anyString());
    }

    @Test
    @DisplayName("Debe ignorar búsqueda con string vacío")
    void testFindAll_WithEmptySearch_IgnoresSearch() {
        // Arrange
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countAllUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(unitOfMeasureServicePort.getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findAll(
                ENTERPRISE_ID, "   ", Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        verify(unitOfMeasureServicePort).countAllUnitOfMeasuresByEntId(ENTERPRISE_ID);
        verify(unitOfMeasureServicePort, never()).countByEntIdAndSearch(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento en búsqueda de unidades")
    void testFindAll_WithSearch_AppliesSorting() {
        // Arrange
        String searchTerm = "Kilo";
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countByEntIdAndSearch(ENTERPRISE_ID, searchTerm)).thenReturn(1L);
        when(unitOfMeasureServicePort.findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), anyInt(), 
                anyInt(), eq("abbreviation"), eq("desc"))).thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.findAll(ENTERPRISE_ID, searchTerm, Optional.of(0), Optional.of(10), "abbreviation", "desc");

        // Assert
        verify(unitOfMeasureServicePort).findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), 
                anyInt(), anyInt(), eq("abbreviation"), eq("desc"));
    }

    // ==================== Tests de findActivate ====================

    @Test
    @DisplayName("Debe obtener unidades de medida activas paginadas")
    void testFindActivate_ReturnsActiveUnits() {
        // Arrange
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findActivate(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10));

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(unitOfMeasureServicePort).countActiveUnitOfMeasuresByEntId(ENTERPRISE_ID);
        verify(unitOfMeasureServicePort).getAllActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Debe usar paginación por defecto en findActivate")
    void testFindActivate_WithoutParams_UsesDefaults() {
        // Arrange
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findActivate(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(unitOfMeasureServicePort).countActiveUnitOfMeasuresByEntId(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe mapear unidades a response en findActivate")
    void testFindActivate_MapsUnitsToResponse() {
        // Arrange
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.findActivate(ENTERPRISE_ID, Optional.of(0), Optional.of(10));

        // Assert
        verify(unitOfMeasureRestMapper).toUnitOfMeasureResponse(any(UnitOfMeasure.class));
    }

    // ==================== Tests de findById ====================

    @Test
    @DisplayName("Debe encontrar unidad de medida por ID")
    void testFindById_ReturnsUnit() {
        // Arrange
        when(unitOfMeasureServicePort.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        UnitOfMeasureResponse result = unitMeasureRestController.findById(UNIT_ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(UNIT_ID, result.getId());
        assertEquals(ENTERPRISE_ID, result.getEnterpriseId());
        assertEquals("Kilogramo", result.getName());
        assertEquals("kg", result.getAbbreviation());
        verify(unitOfMeasureServicePort).findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);
        verify(unitOfMeasureRestMapper).toUnitOfMeasureResponse(unitOfMeasure);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en findById")
    void testFindById_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(unitOfMeasureServicePort.findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.findById(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasureServicePort, times(1)).findByIdAndEnterpriseId(UNIT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de create ====================

    @Test
    @DisplayName("Debe crear unidad de medida exitosamente")
    void testCreate_CreatesUnit() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.create(unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<UnitOfMeasureResponse> result = unitMeasureRestController.create(unitOfMeasureCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(UNIT_ID, result.getBody().getId());
        assertEquals("Kilogramo", result.getBody().getName());
        assertEquals("kg", result.getBody().getAbbreviation());
        verify(unitOfMeasureRestMapper).toUnitOfMeasure(unitOfMeasureCreateRequest);
        verify(unitOfMeasureServicePort).create(unitOfMeasure);
        verify(unitOfMeasureRestMapper).toUnitOfMeasureResponse(unitOfMeasure);
    }

    @Test
    @DisplayName("Debe retornar status 201 al crear unidad de medida")
    void testCreate_ReturnsCreatedStatus() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.create(unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<UnitOfMeasureResponse> result = unitMeasureRestController.create(unitOfMeasureCreateRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear request a domain antes de crear")
    void testCreate_MapsRequestToDomain() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.create(unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.create(unitOfMeasureCreateRequest);

        // Assert
        verify(unitOfMeasureRestMapper).toUnitOfMeasure(unitOfMeasureCreateRequest);
        verify(unitOfMeasureServicePort).create(unitOfMeasure);
    }

    // ==================== Tests de update ====================

    @Test
    @DisplayName("Debe actualizar unidad de medida exitosamente")
    void testUpdate_UpdatesUnit() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.update(UNIT_ID, ENTERPRISE_ID, unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        UnitOfMeasureResponse result = unitMeasureRestController.update(UNIT_ID, unitOfMeasureCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(UNIT_ID, result.getId());
        assertEquals("Kilogramo", result.getName());
        assertEquals("kg", result.getAbbreviation());
        verify(unitOfMeasureRestMapper).toUnitOfMeasure(unitOfMeasureCreateRequest);
        verify(unitOfMeasureServicePort).update(UNIT_ID, ENTERPRISE_ID, unitOfMeasure);
        verify(unitOfMeasureRestMapper).toUnitOfMeasureResponse(unitOfMeasure);
    }

    @Test
    @DisplayName("Deve invocar servicio con parámetros correctos en update")
    void testUpdate_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.update(UNIT_ID, ENTERPRISE_ID, unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.update(UNIT_ID, unitOfMeasureCreateRequest);

        // Assert
        verify(unitOfMeasureServicePort).update(UNIT_ID, ENTERPRISE_ID, unitOfMeasure);
    }

    @Test
    @DisplayName("Debe mapear request a domain antes de actualizar")
    void testUpdate_MapsRequestToDomain() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.update(UNIT_ID, ENTERPRISE_ID, unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.update(UNIT_ID, unitOfMeasureCreateRequest);

        // Assert
        verify(unitOfMeasureRestMapper).toUnitOfMeasure(unitOfMeasureCreateRequest);
    }

    // ==================== Tests de changeState ====================

    @Test
    @DisplayName("Debe cambiar estado de unidad de medida")
    void testChangeState_ChangesState() {
        // Arrange
        doNothing().when(unitOfMeasureServicePort).changeState(UNIT_ID, ENTERPRISE_ID);

        // Act
        unitMeasureRestController.changeState(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasureServicePort).changeState(UNIT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en changeState")
    void testChangeState_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(unitOfMeasureServicePort).changeState(UNIT_ID, ENTERPRISE_ID);

        // Act
        unitMeasureRestController.changeState(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasureServicePort, times(1)).changeState(UNIT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de deleteById ====================

    @Test
    @DisplayName("Debe eliminar unidad de medida por ID")
    void testDeleteById_DeletesUnit() {
        // Arrange
        doNothing().when(unitOfMeasureServicePort).deleteById(UNIT_ID, ENTERPRISE_ID);

        // Act
        unitMeasureRestController.deleteById(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasureServicePort).deleteById(UNIT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en deleteById")
    void testDeleteById_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(unitOfMeasureServicePort).deleteById(UNIT_ID, ENTERPRISE_ID);

        // Act
        unitMeasureRestController.deleteById(UNIT_ID, ENTERPRISE_ID);

        // Assert
        verify(unitOfMeasureServicePort, times(1)).deleteById(UNIT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de integración del controller ====================

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en create")
    void testCreate_MaintainsMapperConsistency() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.create(unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.create(unitOfMeasureCreateRequest);

        // Assert
        verify(unitOfMeasureRestMapper).toUnitOfMeasure(unitOfMeasureCreateRequest);
        verify(unitOfMeasureRestMapper).toUnitOfMeasureResponse(unitOfMeasure);
    }

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en update")
    void testUpdate_MaintainsMapperConsistency() {
        // Arrange
        when(unitOfMeasureRestMapper.toUnitOfMeasure(unitOfMeasureCreateRequest)).thenReturn(unitOfMeasure);
        when(unitOfMeasureServicePort.update(UNIT_ID, ENTERPRISE_ID, unitOfMeasure)).thenReturn(unitOfMeasure);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(unitOfMeasure)).thenReturn(unitOfMeasureResponse);

        // Act
        unitMeasureRestController.update(UNIT_ID, unitOfMeasureCreateRequest);

        // Assert
        verify(unitOfMeasureRestMapper).toUnitOfMeasure(unitOfMeasureCreateRequest);
        verify(unitOfMeasureRestMapper).toUnitOfMeasureResponse(unitOfMeasure);
    }

    @Test
    @DisplayName("Debe manejar lista vacía de unidades correctamente")
    void testFindAll_WithEmptyList_ReturnsEmptyPage() {
        // Arrange
        Page<UnitOfMeasure> emptyPage = Page.empty();
        
        when(unitOfMeasureServicePort.countAllUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(0L);
        when(unitOfMeasureServicePort.getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findAll(
                ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Debe manejar lista vacía de unidades activas correctamente")
    void testFindActivate_WithEmptyList_ReturnsEmptyPage() {
        // Arrange
        Page<UnitOfMeasure> emptyPage = Page.empty();
        
        when(unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(0L);
        when(unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findActivate(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Debe mapear múltiples unidades en lista paginada")
    void testFindAll_MapsMultipleUnits() {
        // Arrange
        UnitOfMeasure unitOfMeasure2 = UnitOfMeasure.builder()
                .id(2L)
                .enterpriseId(ENTERPRISE_ID)
                .name("Litro")
                .abbreviation("L")
                .state(true)
                .build();
        
        List<UnitOfMeasure> unitOfMeasures = List.of(unitOfMeasure, unitOfMeasure2);
        Page<UnitOfMeasure> unitOfMeasurePage = new PageImpl<>(unitOfMeasures);
        
        when(unitOfMeasureServicePort.countAllUnitOfMeasuresByEntId(ENTERPRISE_ID)).thenReturn(2L);
        when(unitOfMeasureServicePort.getAllUnitOfMeasuresByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(unitOfMeasurePage);
        when(unitOfMeasureRestMapper.toUnitOfMeasureResponse(any(UnitOfMeasure.class))).thenReturn(unitOfMeasureResponse);

        // Act
        ResponseEntity<Page<UnitOfMeasureResponse>> result = unitMeasureRestController.findAll(
                ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getBody().getTotalElements());
        verify(unitOfMeasureRestMapper, times(2)).toUnitOfMeasureResponse(any(UnitOfMeasure.class));
    }
}
