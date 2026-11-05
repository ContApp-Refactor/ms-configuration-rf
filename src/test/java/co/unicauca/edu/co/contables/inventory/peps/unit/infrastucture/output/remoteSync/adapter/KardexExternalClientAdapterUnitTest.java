package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.remoteSync.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexBatchDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter.KardexExternalClientAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper.IKardexExternalClientMapper;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

/**
 * @brief Unit tests for KardexExternalClientAdapter
 *
 * Tests the in-process adapter (paso 7): the former RestClient proxy to
 * lb://KARDEX was replaced by a direct call to the kardex module query port.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexExternalClientAdapter Tests")
public class KardexExternalClientAdapterUnitTest {

    private static final String UNEXPECTED_ERROR = "Unexpected error communicating with kardex external service";

    @Mock
    private IKardexExternalClientMapper kardexExternalClientMapper;

    @Mock
    private IKardexQueryPort kardexQueryPort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @InjectMocks
    private KardexExternalClientAdapter kardexExternalClientAdapter;

    private String enterpriseId;
    private co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex sourceKardex;
    private List<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex> sourceRecords;
    private Kardex mappedKardex;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";

        sourceKardex = new co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex();
        sourceKardex.setId(1L);
        sourceKardex.setProductId(1L);
        sourceKardex.setQuantity(10);
        sourceKardex.setUnitPrice(new BigDecimal("100.00"));
        sourceKardex.setFactCode("1");
        sourceKardex.setDetails("Test movement");
        sourceKardex.setType(
                co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType.PURCHASE);

        sourceRecords = new ArrayList<>();
        sourceRecords.add(sourceKardex);

        mappedKardex = Kardex.builder()
                .idKardex(1L)
                .details("Test movement")
                .build();
    }

    @Test
    @DisplayName("Should successfully retrieve kardex records when kardex module returns data")
    void testFindKardexByEnterpriseId_Success() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(sourceRecords);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mappedKardex);

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mappedKardex, result.get(0));
        verify(kardexQueryPort, times(1)).findLastKardexForAllProducts(enterpriseId);
        verify(kardexExternalClientMapper, times(1)).toDomain(any(KardexBatchDTORequest.class));
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should reshape the kardex record into the wire DTO before mapping")
    void testFindKardexByEnterpriseId_MapsToWireDto() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(sourceRecords);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mappedKardex);

        // Act
        kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        ArgumentCaptor<KardexBatchDTORequest> captor = ArgumentCaptor.forClass(KardexBatchDTORequest.class);
        verify(kardexExternalClientMapper).toDomain(captor.capture());
        KardexBatchDTORequest dto = captor.getValue();
        assertEquals(Long.valueOf(1L), dto.getProductId());
        assertEquals(10, dto.getQuantity());
        assertEquals("1", dto.getFactCode());
        assertEquals(0, new BigDecimal("100.00").compareTo(dto.getUnitPrice()));
        assertEquals("Test movement", dto.getDetails());
        assertEquals(MovementType.PURCHASE, dto.getType());
    }

    @Test
    @DisplayName("Should handle multiple kardex records")
    void testFindKardexByEnterpriseId_MultipleRecords() {
        // Arrange
        List<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex> records = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> {
                    co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex record = new co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex();
                    record.setId((long) i);
                    record.setProductId((long) i);
                    record.setQuantity(i);
                    return record;
                })
                .collect(Collectors.toList());
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(records);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mappedKardex);

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertEquals(3, result.size());
        verify(kardexExternalClientMapper, times(3)).toDomain(any(KardexBatchDTORequest.class));
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should return empty list when kardex module returns no records")
    void testFindKardexByEnterpriseId_EmptyResponse() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(List.of());

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(kardexExternalClientMapper, never()).toDomain(any(KardexBatchDTORequest.class));
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should report error and return empty list when kardex module returns null")
    void testFindKardexByEnterpriseId_NullResponse() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(null);

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(500, UNEXPECTED_ERROR);
        verify(kardexExternalClientMapper, never()).toDomain(any(KardexBatchDTORequest.class));
    }

    @Test
    @DisplayName("Should handle query port failure (503 service unavailable)")
    void testFindKardexByEnterpriseId_ServiceUnavailable() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId))
                .thenThrow(new IllegalStateException("503 Service Unavailable"));

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(500, UNEXPECTED_ERROR);
        verify(kardexExternalClientMapper, never()).toDomain(any(KardexBatchDTORequest.class));
    }

    @Test
    @DisplayName("Should handle query port failure (404 not found)")
    void testFindKardexByEnterpriseId_NotFound() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId))
                .thenThrow(new IllegalStateException("404 Not Found"));

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertTrue(result.isEmpty());
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(500, UNEXPECTED_ERROR);
    }

    @Test
    @DisplayName("Should handle query port failure (400 bad request)")
    void testFindKardexByEnterpriseId_BadRequest() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId))
                .thenThrow(new IllegalStateException("400 Bad Request"));

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertTrue(result.isEmpty());
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(500, UNEXPECTED_ERROR);
    }

    @Test
    @DisplayName("Should handle unexpected exceptions")
    void testFindKardexByEnterpriseId_UnexpectedException() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId))
                .thenThrow(new RuntimeException("boom"));

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(formatterResultOutputPort, times(1)).returnErrorGenericResponse(500, UNEXPECTED_ERROR);
        verify(kardexExternalClientMapper, never()).toDomain(any(KardexBatchDTORequest.class));
    }

    @Test
    @DisplayName("Should forward different enterprise IDs to the kardex module")
    void testFindKardexByEnterpriseId_DifferentEnterpriseIds() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts("ENT-777")).thenReturn(List.of());

        // Act
        kardexExternalClientAdapter.findKardexByEnterpriseId("ENT-777");

        // Assert
        verify(kardexQueryPort, times(1)).findLastKardexForAllProducts(eq("ENT-777"));
        verify(kardexQueryPort, never()).findLastKardexForAllProducts(eq(enterpriseId));
    }

    @Test
    @DisplayName("Should handle empty enterprise ID")
    void testFindKardexByEnterpriseId_EmptyEnterpriseId() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts("")).thenReturn(List.of());

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId("");

        // Assert
        assertTrue(result.isEmpty());
        verify(kardexQueryPort, times(1)).findLastKardexForAllProducts("");
    }

    @Test
    @DisplayName("Should handle null enterprise ID gracefully")
    void testFindKardexByEnterpriseId_NullEnterpriseId() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(null)).thenReturn(List.of());

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(kardexQueryPort, times(1)).findLastKardexForAllProducts(null);
    }

    @Test
    @DisplayName("Should handle large number of kardex records")
    void testFindKardexByEnterpriseId_LargeNumberOfRecords() {
        // Arrange
        List<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex> records = IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> {
                    co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex record = new co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex();
                    record.setId((long) i);
                    record.setProductId((long) i);
                    return record;
                })
                .collect(Collectors.toList());
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(records);
        when(kardexExternalClientMapper.toDomain(any(KardexBatchDTORequest.class))).thenReturn(mappedKardex);

        // Act
        List<Kardex> result = kardexExternalClientAdapter.findKardexByEnterpriseId(enterpriseId);

        // Assert
        assertEquals(1000, result.size());
        verify(kardexExternalClientMapper, times(1000)).toDomain(any(KardexBatchDTORequest.class));
        verify(formatterResultOutputPort, never()).returnErrorGenericResponse(anyInt(), anyString());
    }
}
