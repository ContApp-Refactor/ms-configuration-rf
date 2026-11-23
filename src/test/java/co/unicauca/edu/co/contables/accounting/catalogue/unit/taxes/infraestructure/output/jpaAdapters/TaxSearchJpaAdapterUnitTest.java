package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.infraestructure.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.TaxSearchJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxSearchMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxSearchJpaAdapterUnitTest {

    @Mock
    private ITaxRepository taxRepository;

    @Mock
    private ITaxSearchMapper taxSearchMapper;

    @InjectMocks
    private TaxSearchJpaAdapter taxSearchJpaAdapter;

    private String idEnterprise;
    private Long taxId;
    private TaxEntity taxEntity;
    private Tax tax;

    @BeforeEach
    void setUp() {
        idEnterprise = "ENT-001";
        taxId = 1L;

        taxEntity = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();

        tax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
    }

    // ==================== Tests de getTax ====================

    @Test
    @DisplayName("Debe obtener impuesto por código y empresa exitosamente")
    void testGetTaxByCodeSuccess() {
        // Arrange
        String code = "IVA19";
        when(taxRepository.findByCode(code, idEnterprise)).thenReturn(taxEntity);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxSearchJpaAdapter.getTax(code, idEnterprise);

        // Assert
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals(idEnterprise, result.getIdEnterprise());
        verify(taxRepository).findByCode(code, idEnterprise);
        verify(taxSearchMapper).toDomain(taxEntity);
    }

    @Test
    @DisplayName("Debe retornar null cuando el impuesto no existe")
    void testGetTaxReturnsNullWhenNotFound() {
        // Arrange
        String code = "NOEXISTE";
        when(taxRepository.findByCode(code, idEnterprise)).thenReturn(null);
        when(taxSearchMapper.toDomain(null)).thenReturn(null);

        // Act
        Tax result = taxSearchJpaAdapter.getTax(code, idEnterprise);

        // Assert
        assertNull(result);
        verify(taxRepository).findByCode(code, idEnterprise);
    }

    // ==================== Tests de getActiveTaxes ====================

    @Test
    @DisplayName("Debe obtener impuestos activos exitosamente")
    void testGetActiveTaxesSuccess() {
        // Arrange
        List<TaxEntity> activeEntities = Arrays.asList(taxEntity);
        List<Tax> activeTaxes = Arrays.asList(tax);
        when(taxRepository.findActiveByIdEnterprise(idEnterprise)).thenReturn(activeEntities);
        when(taxSearchMapper.toDomainList(activeEntities)).thenReturn(activeTaxes);

        // Act
        List<Tax> result = taxSearchJpaAdapter.getActiveTaxes(idEnterprise);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getStatus());
        verify(taxRepository).findActiveByIdEnterprise(idEnterprise);
        verify(taxSearchMapper).toDomainList(activeEntities);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay impuestos activos")
    void testGetActiveTaxesReturnsEmptyList() {
        // Arrange
        when(taxRepository.findActiveByIdEnterprise(idEnterprise)).thenReturn(Collections.emptyList());
        when(taxSearchMapper.toDomainList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<Tax> result = taxSearchJpaAdapter.getActiveTaxes(idEnterprise);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests de getTaxesByEnterprise ====================

    @Test
    @DisplayName("Debe obtener todos los impuestos por empresa")
    void testGetTaxesByEnterpriseSuccess() {
        // Arrange
        TaxEntity inactiveTax = TaxEntity.builder()
                .id(2L)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .status(false)
                .build();
        List<TaxEntity> allEntities = Arrays.asList(taxEntity, inactiveTax);
        Tax inactiveTaxDomain = Tax.builder().id(2L).code("RET4").status(false).build();
        List<Tax> allTaxes = Arrays.asList(tax, inactiveTaxDomain);
        when(taxRepository.findAllByIdEnterprise(idEnterprise)).thenReturn(allEntities);
        when(taxSearchMapper.toDomainList(allEntities)).thenReturn(allTaxes);

        // Act
        List<Tax> result = taxSearchJpaAdapter.getTaxesByEnterprise(idEnterprise);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taxRepository).findAllByIdEnterprise(idEnterprise);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay impuestos en la empresa")
    void testGetTaxesByEnterpriseReturnsEmptyList() {
        // Arrange
        when(taxRepository.findAllByIdEnterprise(idEnterprise)).thenReturn(Collections.emptyList());
        when(taxSearchMapper.toDomainList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<Tax> result = taxSearchJpaAdapter.getTaxesByEnterprise(idEnterprise);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests de getTaxByIdAndEnterprise ====================

    @Test
    @DisplayName("Debe obtener impuesto por ID y empresa exitosamente")
    void testGetTaxByIdAndEnterpriseSuccess() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxSearchJpaAdapter.getTaxByIdAndEnterprise(taxId, idEnterprise);

        // Assert
        assertNotNull(result);
        assertEquals(taxId, result.getId());
        assertEquals(idEnterprise, result.getIdEnterprise());
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe retornar null cuando el impuesto no pertenece a la empresa")
    void testGetTaxByIdAndEnterpriseReturnsNullWhenNotFound() {
        // Arrange
        String differentEnterprise = "ENT-002";
        when(taxRepository.findByIdAndIdEnterprise(taxId, differentEnterprise)).thenReturn(null);
        when(taxSearchMapper.toDomain(null)).thenReturn(null);

        // Act
        Tax result = taxSearchJpaAdapter.getTaxByIdAndEnterprise(taxId, differentEnterprise);

        // Assert
        assertNull(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, differentEnterprise);
    }

    // ==================== Tests de getTaxesPaginated ====================

    @Test
    @DisplayName("Debe obtener impuestos paginados con orden ascendente")
    void testGetTaxesPaginatedAscending() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortField = "description";
        String sortOrder = "asc";
        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortField));
        Page<TaxEntity> entityPage = new PageImpl<>(Arrays.asList(taxEntity), expectedPageable, 1);
        when(taxRepository.findAllByIdEnterprise(eq(idEnterprise), any(Pageable.class))).thenReturn(entityPage);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        Page<Tax> result = taxSearchJpaAdapter.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(taxRepository).findAllByIdEnterprise(eq(idEnterprise), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener impuestos paginados con orden descendente")
    void testGetTaxesPaginatedDescending() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortField = "code";
        String sortOrder = "desc";
        Page<TaxEntity> entityPage = new PageImpl<>(Arrays.asList(taxEntity));
        when(taxRepository.findAllByIdEnterprise(eq(idEnterprise), any(Pageable.class))).thenReturn(entityPage);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        Page<Tax> result = taxSearchJpaAdapter.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        verify(taxRepository).findAllByIdEnterprise(eq(idEnterprise), argThat(pageable ->
            pageable.getSort().getOrderFor(sortField).getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay impuestos")
    void testGetTaxesPaginatedReturnsEmptyPage() {
        // Arrange
        Page<TaxEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(taxRepository.findAllByIdEnterprise(eq(idEnterprise), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<Tax> result = taxSearchJpaAdapter.getTaxesPaginated(idEnterprise, 0, 10, "description", "asc");

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Debe usar orden ascendente por defecto cuando sortOrder no es desc")
    void testGetTaxesPaginatedDefaultsToAscending() {
        // Arrange
        String sortOrder = "invalid";
        Page<TaxEntity> entityPage = new PageImpl<>(Arrays.asList(taxEntity));
        when(taxRepository.findAllByIdEnterprise(eq(idEnterprise), any(Pageable.class))).thenReturn(entityPage);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        taxSearchJpaAdapter.getTaxesPaginated(idEnterprise, 0, 10, "description", sortOrder);

        // Assert
        verify(taxRepository).findAllByIdEnterprise(eq(idEnterprise), argThat(pageable ->
            pageable.getSort().getOrderFor("description").getDirection() == Sort.Direction.ASC
        ));
    }

    // ==================== Tests de getTaxesByCodeOrDescriptionPaginated ====================

    @Test
    @DisplayName("Debe obtener impuestos filtrados por búsqueda con paginación ascendente")
    void testGetTaxesByCodeOrDescriptionPaginatedAscending() {
        // Arrange
        String search = "IVA";
        int page = 0;
        int size = 10;
        String sortField = "description";
        String sortOrder = "asc";
        Page<TaxEntity> entityPage = new PageImpl<>(Arrays.asList(taxEntity));
        when(taxRepository.findByIdEnterpriseAndDescriptionContainingIgnoreCase(eq(idEnterprise), eq(search), any(Pageable.class)))
            .thenReturn(entityPage);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        Page<Tax> result = taxSearchJpaAdapter.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(taxRepository).findByIdEnterpriseAndDescriptionContainingIgnoreCase(eq(idEnterprise), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener impuestos filtrados por búsqueda con paginación descendente")
    void testGetTaxesByCodeOrDescriptionPaginatedDescending() {
        // Arrange
        String search = "IVA";
        String sortOrder = "desc";
        Page<TaxEntity> entityPage = new PageImpl<>(Arrays.asList(taxEntity));
        when(taxRepository.findByIdEnterpriseAndDescriptionContainingIgnoreCase(eq(idEnterprise), eq(search), any(Pageable.class)))
            .thenReturn(entityPage);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        taxSearchJpaAdapter.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, 0, 10, "code", sortOrder);

        // Assert
        verify(taxRepository).findByIdEnterpriseAndDescriptionContainingIgnoreCase(eq(idEnterprise), eq(search), argThat(pageable ->
            pageable.getSort().getOrderFor("code").getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando búsqueda no encuentra resultados")
    void testGetTaxesByCodeOrDescriptionPaginatedReturnsEmptyPage() {
        // Arrange
        String search = "NOEXISTE";
        Page<TaxEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(taxRepository.findByIdEnterpriseAndDescriptionContainingIgnoreCase(eq(idEnterprise), eq(search), any(Pageable.class)))
            .thenReturn(emptyPage);

        // Act
        Page<Tax> result = taxSearchJpaAdapter.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, 0, 10, "description", "asc");

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de countTaxesByEnterprise ====================

    @Test
    @DisplayName("Debe contar impuestos por empresa correctamente")
    void testCountTaxesByEnterpriseSuccess() {
        // Arrange
        long expectedCount = 5L;
        when(taxRepository.countByIdEnterprise(idEnterprise)).thenReturn(expectedCount);

        // Act
        long result = taxSearchJpaAdapter.countTaxesByEnterprise(idEnterprise);

        // Assert
        assertEquals(expectedCount, result);
        verify(taxRepository).countByIdEnterprise(idEnterprise);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay impuestos en la empresa")
    void testCountTaxesByEnterpriseReturnsZero() {
        // Arrange
        when(taxRepository.countByIdEnterprise(idEnterprise)).thenReturn(0L);

        // Act
        long result = taxSearchJpaAdapter.countTaxesByEnterprise(idEnterprise);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests de countTaxesByEnterpriseAndCodeOrDescription ====================

    @Test
    @DisplayName("Debe contar impuestos filtrados por búsqueda correctamente")
    void testCountTaxesByEnterpriseAndCodeOrDescriptionSuccess() {
        // Arrange
        String search = "IVA";
        long expectedCount = 3L;
        when(taxRepository.countByIdEnterpriseAndDescriptionContainingIgnoreCase(idEnterprise, search)).thenReturn(expectedCount);

        // Act
        long result = taxSearchJpaAdapter.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);

        // Assert
        assertEquals(expectedCount, result);
        verify(taxRepository).countByIdEnterpriseAndDescriptionContainingIgnoreCase(idEnterprise, search);
    }

    @Test
    @DisplayName("Debe retornar cero cuando búsqueda no encuentra resultados")
    void testCountTaxesByEnterpriseAndCodeOrDescriptionReturnsZero() {
        // Arrange
        String search = "NOEXISTE";
        when(taxRepository.countByIdEnterpriseAndDescriptionContainingIgnoreCase(idEnterprise, search)).thenReturn(0L);

        // Act
        long result = taxSearchJpaAdapter.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests adicionales de cobertura ====================

    @Test
    @DisplayName("Debe manejar múltiples impuestos activos")
    void testGetActiveTaxesWithMultipleTaxes() {
        // Arrange
        TaxEntity taxEntity2 = TaxEntity.builder()
                .id(2L)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .build();
        Tax tax2 = Tax.builder()
                .id(2L)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .build();
        List<TaxEntity> activeEntities = Arrays.asList(taxEntity, taxEntity2);
        List<Tax> activeTaxes = Arrays.asList(tax, tax2);
        when(taxRepository.findActiveByIdEnterprise(idEnterprise)).thenReturn(activeEntities);
        when(taxSearchMapper.toDomainList(activeEntities)).thenReturn(activeTaxes);

        // Act
        List<Tax> result = taxSearchJpaAdapter.getActiveTaxes(idEnterprise);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Debe manejar diferentes páginas en paginación")
    void testGetTaxesPaginatedWithDifferentPages() {
        // Arrange
        int page = 2;
        int size = 5;
        Page<TaxEntity> entityPage = new PageImpl<>(Arrays.asList(taxEntity), PageRequest.of(page, size), 15);
        when(taxRepository.findAllByIdEnterprise(eq(idEnterprise), any(Pageable.class))).thenReturn(entityPage);
        when(taxSearchMapper.toDomain(taxEntity)).thenReturn(tax);

        // Act
        Page<Tax> result = taxSearchJpaAdapter.getTaxesPaginated(idEnterprise, page, size, "description", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(15, result.getTotalElements());
        verify(taxRepository).findAllByIdEnterprise(eq(idEnterprise), argThat(pageable ->
            pageable.getPageNumber() == page && pageable.getPageSize() == size
        ));
    }
}
