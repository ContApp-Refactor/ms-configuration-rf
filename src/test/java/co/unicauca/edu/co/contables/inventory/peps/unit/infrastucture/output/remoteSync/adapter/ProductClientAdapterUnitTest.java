package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductSyncServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter.ProductClientAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

/**
 * @brief Unit tests for ProductClientAdapter
 *
 * Antes de la migración este adaptador llamaba por HTTP a {@code lb://PRODUCTS}.
 * Ahora delega en {@link IProductSyncServicePort} (mismo proceso) y proyecta el
 * DTO de products sobre el DTO local de peps, descartando {@code quantity} y
 * {@code cost} que peps no usa.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductClientAdapter Tests")
class ProductClientAdapterUnitTest {

    @Mock
    private IProductClientMapper productClientMapper;

    @Mock
    private IProductSyncServicePort productSyncServicePort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @InjectMocks
    private ProductClientAdapter productClientAdapter;

    private String enterpriseId;
    private Instant since;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        since = Instant.parse("2024-11-01T00:00:00Z");
    }

    private ProductSyncDto sourceProduct(Long id, String name) {
        ProductSyncDto dto = new ProductSyncDto();
        dto.setProductId(id);
        dto.setName(name);
        dto.setReference("REF-" + id);
        dto.setEnterpriseId(enterpriseId);
        dto.setPresentation("caja");
        dto.setQuantity(99);
        dto.setCost(1234.5);
        dto.setState(true);
        return dto;
    }

    @Test
    @DisplayName("Should return the mapped products when the in-process service answers")
    void testFindAllProducts_Success() {
        ProductSyncDto source = sourceProduct(10L, "Papel");
        Product expected = new Product();
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since)).thenReturn(List.of(source));
        when(productClientMapper.toDomain(any())).thenReturn(expected);

        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        assertEquals(1, result.size());
        assertEquals(expected, result.get(0));
        verify(productSyncServicePort, times(1)).findByEnterpriseId(enterpriseId, since);
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should preserve the service order")
    void testFindAllProducts_PreservesOrder() {
        ProductSyncDto first = sourceProduct(1L, "Primero");
        ProductSyncDto second = sourceProduct(2L, "Segundo");
        Product mappedFirst = new Product();
        Product mappedSecond = new Product();

        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since)).thenReturn(List.of(first, second));
        when(productClientMapper.toDomain(any())).thenReturn(mappedFirst, mappedSecond);

        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        assertEquals(2, result.size());
        assertEquals(mappedFirst, result.get(0));
        assertEquals(mappedSecond, result.get(1));
    }

    @Test
    @DisplayName("Should return an empty list when there are no products")
    void testFindAllProducts_EmptyList() {
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since)).thenReturn(List.of());

        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productClientMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should keep the enterprise and the timestamp untouched")
    void testFindAllProducts_ParameterVerification() {
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since)).thenReturn(List.of());

        productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        verify(productSyncServicePort, times(1)).findByEnterpriseId(eq(enterpriseId), eq(since));
    }

    @Test
    @DisplayName("Should project only the fields peps shares with products")
    void testFindAllProducts_ProjectionDropsQuantityAndCost() {
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(List.of(sourceProduct(77L, "Toner")));
        when(productClientMapper.toDomain(any())).thenReturn(new Product());

        productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto>
            captor = ArgumentCaptor.forClass(
                co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto.class);
        verify(productClientMapper, times(1)).toDomain(captor.capture());

        var projected = captor.getValue();
        assertEquals(77L, projected.getProductId());
        assertEquals("Toner", projected.getName());
        assertEquals("REF-77", projected.getReference());
        assertEquals(enterpriseId, projected.getEnterpriseId());
        assertEquals("caja", projected.getPresentation());
        assertTrue(projected.isState());
        // quantity y cost no existen en el DTO de peps: son los campos descartados.
    }

    @Test
    @DisplayName("Should swallow a failure and report 500 when the product service throws")
    void testFindAllProducts_ServiceThrows_ReportsGenericError() {
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenThrow(new IllegalStateException("products unavailable"));

        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        assertTrue(result.isEmpty());
        verify(formatterResultOutputPort)
            .returnErrorGenericResponse(500, "Unexpected error communicating with product service");
    }

    @Test
    @DisplayName("Should swallow a failure and report 500 when the mapper throws")
    void testFindAllProducts_MapperThrows_ReportsGenericError() {
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenReturn(List.of(sourceProduct(1L, "Papel")));
        when(productClientMapper.toDomain(any())).thenThrow(new IllegalStateException("mapper failure"));

        List<Product> result = productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since);

        assertTrue(result.isEmpty());
        verify(formatterResultOutputPort)
            .returnErrorGenericResponse(500, "Unexpected error communicating with product service");
    }

    @Test
    @DisplayName("Should propagate the error when the formatter itself refuses the request")
    void testFindAllProducts_FormatterThrows_PropagatesError() {
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since))
            .thenThrow(new IllegalStateException("products unavailable"));
        doThrow(new GenericErrorException(500, "Unexpected error communicating with product service"))
            .when(formatterResultOutputPort).returnErrorGenericResponse(anyInt(), anyString());

        GenericErrorException thrown = assertThrows(GenericErrorException.class,
            () -> productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since));

        assertEquals("Unexpected error communicating with product service", thrown.getMessage());
    }

    @Test
    @DisplayName("Should support different enterprise IDs independently")
    void testFindAllProducts_DifferentEnterprises() {
        String otherEnterprise = "ENT-999";
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since)).thenReturn(List.of(sourceProduct(1L, "A")));
        when(productSyncServicePort.findByEnterpriseId(otherEnterprise, since)).thenReturn(List.of());
        when(productClientMapper.toDomain(any())).thenReturn(new Product());

        assertEquals(1, productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since).size());
        assertTrue(productClientAdapter.findAllProductsByEnterpriseId(otherEnterprise, since).isEmpty());
    }

    @Test
    @DisplayName("Should support different timestamps independently")
    void testFindAllProducts_DifferentTimestamps() {
        Instant otherSince = Instant.parse("2025-01-01T00:00:00Z");
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, since)).thenReturn(List.of(sourceProduct(1L, "A")));
        when(productSyncServicePort.findByEnterpriseId(enterpriseId, otherSince)).thenReturn(List.of());

        assertEquals(1, productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, since).size());
        assertTrue(productClientAdapter.findAllProductsByEnterpriseId(enterpriseId, otherSince).isEmpty());
    }
}
