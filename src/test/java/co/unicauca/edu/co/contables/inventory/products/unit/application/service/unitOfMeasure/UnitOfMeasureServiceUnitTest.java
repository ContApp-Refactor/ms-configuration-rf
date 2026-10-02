package co.unicauca.edu.co.contables.inventory.products.unit.application.service.unitOfMeasure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.product.ProductService;
import co.unicauca.edu.co.contables.inventory.products.application.service.unitOfMeasure.UnitOfMeasureService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureAbbreviationAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureAssociatedException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnitOfMeasureServiceUnitTest {

    @Mock
    private IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;

    @Mock
    private ProductService productService;

    @InjectMocks
    private UnitOfMeasureService unitOfMeasureService;

    private UnitOfMeasure unitOfMeasure;
    private String enterpriseId;
    private Long unitOfMeasureId;

    @BeforeEach
    void setUp() {
        enterpriseId = "enterprise-123";
        unitOfMeasureId = 1L;

        unitOfMeasure = UnitOfMeasure.builder()
                .id(unitOfMeasureId)
                .name("Kilogramo")
                .abbreviation("Kg")
                .description("Unidad de masa")
                .enterpriseId(enterpriseId)
                .state(true)
                .build();
    }

    @Test
    @DisplayName("Debe encontrar unidad de medida por ID y empresa exitosamente")
    void testFindByIdAndEnterpriseIdSuccess() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));

        // Act
        UnitOfMeasure result = unitOfMeasureService.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(unitOfMeasureId, result.getId());
        assertEquals(enterpriseId, result.getEnterpriseId());
        verify(unitOfMeasurePersistencePort, times(1)).findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando unidad de medida no existe")
    void testFindByIdAndEnterpriseIdNotFound() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnitOfMeasureNotFoundException.class, () ->
                unitOfMeasureService.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId)
        );
    }

    @Test
    @DisplayName("Debe crear unidad de medida exitosamente")
    void testCreateSuccess() {
        // Arrange
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.existsByAbbreviationAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.create(any(UnitOfMeasure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UnitOfMeasure result = unitOfMeasureService.create(unitOfMeasure);

        // Assert
        assertNotNull(result);
        assertEquals("Kilogramo", result.getName());
        assertEquals("Kg", result.getAbbreviation());
        verify(unitOfMeasurePersistencePort, times(1)).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear con nombre duplicado")
    void testCreateWithDuplicateName() {
        // Arrange
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UnitOfMeasureNameAlreadyExistsException.class, () ->
                unitOfMeasureService.create(unitOfMeasure)
        );
        verify(unitOfMeasurePersistencePort, never()).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear con abreviación duplicada")
    void testCreateWithDuplicateAbbreviation() {
        // Arrange
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.existsByAbbreviationAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UnitOfMeasureAbbreviationAlreadyExistsException.class, () ->
                unitOfMeasureService.create(unitOfMeasure)
        );
        verify(unitOfMeasurePersistencePort, never()).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe normalizar nombre y abreviación al crear")
    void testCreateNormalizesNameAndAbbreviation() {
        // Arrange
        unitOfMeasure.setName("  kilogramo  ");
        unitOfMeasure.setAbbreviation("  kg  ");
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.existsByAbbreviationAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.create(any(UnitOfMeasure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UnitOfMeasure result = unitOfMeasureService.create(unitOfMeasure);

        // Assert
        assertNotNull(result);
        assertEquals("Kilogramo", result.getName());
        assertEquals("Kg", result.getAbbreviation());
    }

    @Test
    @DisplayName("Debe actualizar unidad de medida exitosamente")
    void testUpdateSuccess() {
        // Arrange
        UnitOfMeasure updateData = UnitOfMeasure.builder()
                .name("Gramo")
                .abbreviation("G")
                .description("Nueva descripción")
                .enterpriseId(enterpriseId)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Collections.emptyList());
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.existsByAbbreviationAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.create(any(UnitOfMeasure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UnitOfMeasure result = unitOfMeasureService.update(unitOfMeasureId, enterpriseId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals("Gramo", result.getName());
        assertEquals("G", result.getAbbreviation());
        verify(unitOfMeasurePersistencePort, times(1)).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar unidad que no existe")
    void testUpdateNotFound() {
        // Arrange
        UnitOfMeasure updateData = UnitOfMeasure.builder()
                .name("Gramo")
                .abbreviation("G")
                .enterpriseId(enterpriseId)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnitOfMeasureNotFoundException.class, () ->
                unitOfMeasureService.update(unitOfMeasureId, enterpriseId, updateData)
        );
        verify(unitOfMeasurePersistencePort, never()).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar unidad en uso")
    void testUpdateUnitInUse() {
        // Arrange
        UnitOfMeasure updateData = UnitOfMeasure.builder()
                .name("Gramo")
                .abbreviation("G")
                .enterpriseId(enterpriseId)
                .build();

        Product productInUse = Product.builder()
                .id(1L)
                .usageCount(5)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Arrays.asList(productInUse));

        // Act & Assert
        assertThrows(UnitOfMeasureInUseException.class, () ->
                unitOfMeasureService.update(unitOfMeasureId, enterpriseId, updateData)
        );
        verify(unitOfMeasurePersistencePort, never()).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con nombre duplicado en otro registro")
    void testUpdateWithDuplicateName() {
        // Arrange
        UnitOfMeasure updateData = UnitOfMeasure.builder()
                .name("Nombre Existente")
                .abbreviation("G")
                .enterpriseId(enterpriseId)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Collections.emptyList());
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UnitOfMeasureNameAlreadyExistsException.class, () ->
                unitOfMeasureService.update(unitOfMeasureId, enterpriseId, updateData)
        );
        verify(unitOfMeasurePersistencePort, never()).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con abreviación duplicada en otro registro")
    void testUpdateWithDuplicateAbbreviation() {
        // Arrange
        UnitOfMeasure updateData = UnitOfMeasure.builder()
                .name("Gramo")
                .abbreviation("ABV")
                .enterpriseId(enterpriseId)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Collections.emptyList());
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.existsByAbbreviationAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UnitOfMeasureAbbreviationAlreadyExistsException.class, () ->
                unitOfMeasureService.update(unitOfMeasureId, enterpriseId, updateData)
        );
        verify(unitOfMeasurePersistencePort, never()).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe normalizar nombre y abreviación al actualizar")
    void testUpdateNormalizesNameAndAbbreviation() {
        // Arrange
        UnitOfMeasure updateData = UnitOfMeasure.builder()
                .name("  gramo  ")
                .abbreviation("  g  ")
                .enterpriseId(enterpriseId)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Collections.emptyList());
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.existsByAbbreviationAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.create(any(UnitOfMeasure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UnitOfMeasure result = unitOfMeasureService.update(unitOfMeasureId, enterpriseId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals("Gramo", result.getName());
        assertEquals("G", result.getAbbreviation());
    }

    @Test
    @DisplayName("Debe permitir actualizar cuando productos asociados no están en uso")
    void testUpdateWithProductsNotInUse() {
        // Arrange
        UnitOfMeasure updateData = UnitOfMeasure.builder()
                .name("Gramo")
                .abbreviation("G")
                .enterpriseId(enterpriseId)
                .build();

        Product productNotInUse = Product.builder()
                .id(1L)
                .usageCount(0)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Arrays.asList(productNotInUse));
        when(unitOfMeasurePersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.existsByAbbreviationAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(unitOfMeasureId)))
                .thenReturn(false);
        when(unitOfMeasurePersistencePort.create(any(UnitOfMeasure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UnitOfMeasure result = unitOfMeasureService.update(unitOfMeasureId, enterpriseId, updateData);

        // Assert
        assertNotNull(result);
        verify(unitOfMeasurePersistencePort, times(1)).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe cambiar estado de activo a inactivo")
    void testChangeStateFromActiveToInactive() {
        // Arrange
        unitOfMeasure.setState(true);
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(unitOfMeasurePersistencePort.create(any(UnitOfMeasure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        unitOfMeasureService.changeState(unitOfMeasureId, enterpriseId);

        // Assert
        verify(unitOfMeasurePersistencePort, times(1)).create(any(UnitOfMeasure.class));
        assertEquals(false, unitOfMeasure.isState());
    }

    @Test
    @DisplayName("Debe cambiar estado de inactivo a activo")
    void testChangeStateFromInactiveToActive() {
        // Arrange
        unitOfMeasure.setState(false);
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(unitOfMeasurePersistencePort.create(any(UnitOfMeasure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        unitOfMeasureService.changeState(unitOfMeasureId, enterpriseId);

        // Assert
        verify(unitOfMeasurePersistencePort, times(1)).create(any(UnitOfMeasure.class));
        assertEquals(true, unitOfMeasure.isState());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado de unidad que no existe")
    void testChangeStateNotFound() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnitOfMeasureNotFoundException.class, () ->
                unitOfMeasureService.changeState(unitOfMeasureId, enterpriseId)
        );
        verify(unitOfMeasurePersistencePort, never()).create(any(UnitOfMeasure.class));
    }

    @Test
    @DisplayName("Debe eliminar unidad de medida exitosamente")
    void testDeleteByIdSuccess() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Collections.emptyList());

        // Act
        unitOfMeasureService.deleteById(unitOfMeasureId, enterpriseId);

        // Assert
        verify(unitOfMeasurePersistencePort, times(1)).deleteByIdAndEnterpriseId(unitOfMeasureId, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar unidad que no existe")
    void testDeleteByIdNotFound() {
        // Arrange
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnitOfMeasureNotFoundException.class, () ->
                unitOfMeasureService.deleteById(unitOfMeasureId, enterpriseId)
        );
        verify(unitOfMeasurePersistencePort, never()).deleteByIdAndEnterpriseId(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar unidad con productos asociados")
    void testDeleteByIdWithAssociatedProducts() {
        // Arrange
        Product associatedProduct = Product.builder()
                .id(1L)
                .unitOfMeasureId(unitOfMeasureId)
                .build();

        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitOfMeasureId, enterpriseId))
                .thenReturn(Optional.of(unitOfMeasure));
        when(productService.findAllByUnitOfMeasure(unitOfMeasureId))
                .thenReturn(Arrays.asList(associatedProduct));

        // Act & Assert
        assertThrows(UnitOfMeasureAssociatedException.class, () ->
                unitOfMeasureService.deleteById(unitOfMeasureId, enterpriseId)
        );
        verify(unitOfMeasurePersistencePort, never()).deleteByIdAndEnterpriseId(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe contar todas las unidades de medida por empresa")
    void testCountAllUnitOfMeasuresByEntId() {
        // Arrange
        when(unitOfMeasurePersistencePort.countByEnterpriseId(enterpriseId))
                .thenReturn(10L);

        // Act
        long result = unitOfMeasureService.countAllUnitOfMeasuresByEntId(enterpriseId);

        // Assert
        assertEquals(10L, result);
        verify(unitOfMeasurePersistencePort, times(1)).countByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Debe buscar unidades de medida por empresa y término de búsqueda")
    void testFindByEntIdAndSearch() {
        // Arrange
        List<UnitOfMeasure> units = Arrays.asList(unitOfMeasure);
        Page<UnitOfMeasure> page = new PageImpl<>(units);
        when(unitOfMeasurePersistencePort.findByEnterpriseIdAndSearch(enterpriseId, "kilo", 0, 10, "name", "asc"))
                .thenReturn(page);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasureService.findByEntIdAndSearch(enterpriseId, "kilo", 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(unitOfMeasurePersistencePort, times(1)).findByEnterpriseIdAndSearch(enterpriseId, "kilo", 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe contar unidades de medida por empresa y búsqueda")
    void testCountByEntIdAndSearch() {
        // Arrange
        when(unitOfMeasurePersistencePort.countByEnterpriseIdAndSearch(enterpriseId, "kilo"))
                .thenReturn(3L);

        // Act
        long result = unitOfMeasureService.countByEntIdAndSearch(enterpriseId, "kilo");

        // Assert
        assertEquals(3L, result);
        verify(unitOfMeasurePersistencePort, times(1)).countByEnterpriseIdAndSearch(enterpriseId, "kilo");
    }

    @Test
    @DisplayName("Debe obtener unidades de medida paginadas con ordenamiento")
    void testGetAllUnitOfMeasuresByWithSort() {
        // Arrange
        List<UnitOfMeasure> units = Arrays.asList(unitOfMeasure);
        Page<UnitOfMeasure> page = new PageImpl<>(units);
        when(unitOfMeasurePersistencePort.getAllUnitOfMeasuresByWithSort(enterpriseId, 0, 10, "name", "asc"))
                .thenReturn(page);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasureService.getAllUnitOfMeasuresByWithSort(enterpriseId, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(unitOfMeasurePersistencePort, times(1)).getAllUnitOfMeasuresByWithSort(enterpriseId, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe obtener unidades de medida activas")
    void testGetAllActiveUnitOfMeasuresBy() {
        // Arrange
        List<UnitOfMeasure> units = Arrays.asList(unitOfMeasure);
        Page<UnitOfMeasure> page = new PageImpl<>(units);
        when(unitOfMeasurePersistencePort.getActiveUnitOfMeasuresBy(enterpriseId, 0, 10, "name", "asc"))
                .thenReturn(page);

        // Act
        Page<UnitOfMeasure> result = unitOfMeasureService.getAllActiveUnitOfMeasuresBy(enterpriseId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(unitOfMeasurePersistencePort, times(1)).getActiveUnitOfMeasuresBy(enterpriseId, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe contar unidades de medida activas por empresa")
    void testCountActiveUnitOfMeasuresByEntId() {
        // Arrange
        when(unitOfMeasurePersistencePort.countActiveByEnterpriseId(enterpriseId))
                .thenReturn(7L);

        // Act
        long result = unitOfMeasureService.countActiveUnitOfMeasuresByEntId(enterpriseId);

        // Assert
        assertEquals(7L, result);
        verify(unitOfMeasurePersistencePort, times(1)).countActiveByEnterpriseId(enterpriseId);
    }
}
