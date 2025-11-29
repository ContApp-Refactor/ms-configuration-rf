package co.unicauca.edu.co.contables.configuration.unit.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterExportNoDataException;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ExportCostCenterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExportCostCenterServiceImplUnitTest {

    @Mock
    private CostCenterRepository costCenterRepository;

    @InjectMocks
    private ExportCostCenterServiceImpl service;

    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Long ID = 1L;
    private static final String CODE = "1001";
    private static final String NAME = "Centro de costo";

    private CostCenterEntity entity;
    private CostCenterEntity entityInactive;

    @BeforeEach
    void setUp() {
        entity = CostCenterEntity.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .code(CODE)
                .name(NAME)
                .status(true)
                .usageCount(0)
                .build();

        entityInactive = CostCenterEntity.builder()
                .id(2L)
                .idEnterprise(ENTERPRISE_ID)
                .code("1002")
                .name("Centro inactivo")
                .status(false)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("exportCostCenters - Debe exportar todos los centros de costo cuando status es null")
    void testExportCostCentersAllWhenStatusIsNull() throws IOException {
        when(costCenterRepository.findAllByIdEnterprise(ENTERPRISE_ID)).thenReturn(List.of(entity, entityInactive));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, null);

        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
        verify(costCenterRepository).findAllByIdEnterprise(ENTERPRISE_ID);
        verify(costCenterRepository, never()).findAllByIdEnterpriseAndStatus(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("exportCostCenters - Debe exportar solo centros de costo activos cuando status es true")
    void testExportCostCentersActiveOnly() throws IOException {
        when(costCenterRepository.findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(List.of(entity));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, true);

        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
        verify(costCenterRepository).findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, true);
        verify(costCenterRepository, never()).findAllByIdEnterprise(anyString());
    }

    @Test
    @DisplayName("exportCostCenters - Debe exportar solo centros de costo inactivos cuando status es false")
    void testExportCostCentersInactiveOnly() throws IOException {
        when(costCenterRepository.findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, false)).thenReturn(List.of(entityInactive));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, false);

        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
        verify(costCenterRepository).findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, false);
    }

    @Test
    @DisplayName("exportCostCenters - Debe lanzar excepción cuando no hay datos con status null")
    void testExportCostCentersThrowsExceptionWhenNoDataWithNullStatus() {
        when(costCenterRepository.findAllByIdEnterprise(ENTERPRISE_ID)).thenReturn(Collections.emptyList());

        assertThrows(CostCenterExportNoDataException.class, () -> service.exportCostCenters(ENTERPRISE_ID, null));

        verify(costCenterRepository).findAllByIdEnterprise(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("exportCostCenters - Debe lanzar excepción cuando no hay centros activos")
    void testExportCostCentersThrowsExceptionWhenNoActiveData() {
        when(costCenterRepository.findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(Collections.emptyList());

        assertThrows(CostCenterExportNoDataException.class, () -> service.exportCostCenters(ENTERPRISE_ID, true));

        verify(costCenterRepository).findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, true);
    }

    @Test
    @DisplayName("exportCostCenters - Debe lanzar excepción cuando no hay centros inactivos")
    void testExportCostCentersThrowsExceptionWhenNoInactiveData() {
        when(costCenterRepository.findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, false)).thenReturn(Collections.emptyList());

        assertThrows(CostCenterExportNoDataException.class, () -> service.exportCostCenters(ENTERPRISE_ID, false));

        verify(costCenterRepository).findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, false);
    }

    @Test
    @DisplayName("exportCostCenters - Debe generar archivo Excel válido")
    void testExportCostCentersGeneratesValidExcelFile() throws IOException {
        when(costCenterRepository.findAllByIdEnterprise(ENTERPRISE_ID)).thenReturn(List.of(entity));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, null);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isReadable());
        
        try (InputStream inputStream = result.getInputStream()) {
            assertNotNull(inputStream);
            byte[] bytes = inputStream.readAllBytes();
            assertTrue(bytes.length > 0);
            assertEquals((byte) 0x50, bytes[0]);
            assertEquals((byte) 0x4B, bytes[1]);
        }
    }

    @Test
    @DisplayName("exportCostCenters - Debe exportar múltiples centros de costo correctamente")
    void testExportCostCentersMultipleEntities() throws IOException {
        CostCenterEntity entity2 = CostCenterEntity.builder()
                .id(3L)
                .idEnterprise(ENTERPRISE_ID)
                .code("1003")
                .name("Otro centro")
                .status(true)
                .usageCount(0)
                .build();
        CostCenterEntity entity3 = CostCenterEntity.builder()
                .id(4L)
                .idEnterprise(ENTERPRISE_ID)
                .code("1004")
                .name("Tercer centro")
                .status(true)
                .usageCount(0)
                .build();
        when(costCenterRepository.findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, true))
                .thenReturn(List.of(entity, entity2, entity3));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, true);

        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
    }

    @Test
    @DisplayName("exportCostCenters - Debe manejar centro de costo con nombre largo")
    void testExportCostCentersWithLongName() throws IOException {
        CostCenterEntity entityLongName = CostCenterEntity.builder()
                .id(5L)
                .idEnterprise(ENTERPRISE_ID)
                .code("1005")
                .name("Este es un nombre de centro de costo extremadamente largo para probar que el Excel maneja correctamente nombres extensos")
                .status(true)
                .usageCount(0)
                .build();
        when(costCenterRepository.findAllByIdEnterprise(ENTERPRISE_ID)).thenReturn(List.of(entityLongName));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, null);

        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
    }

    @Test
    @DisplayName("exportCostCenters - Debe manejar centro de costo con código corto")
    void testExportCostCentersWithShortCode() throws IOException {
        CostCenterEntity entityShortCode = CostCenterEntity.builder()
                .id(6L)
                .idEnterprise(ENTERPRISE_ID)
                .code("10")
                .name("Centro raíz")
                .status(true)
                .usageCount(0)
                .build();
        when(costCenterRepository.findAllByIdEnterprise(ENTERPRISE_ID)).thenReturn(List.of(entityShortCode));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, null);

        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
    }

    @Test
    @DisplayName("exportCostCenters - Debe incluir centros activos e inactivos cuando status es null")
    void testExportCostCentersIncludesBothStatuses() throws IOException {
        when(costCenterRepository.findAllByIdEnterprise(ENTERPRISE_ID)).thenReturn(List.of(entity, entityInactive));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, null);

        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
        verify(costCenterRepository).findAllByIdEnterprise(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("exportCostCenters - Debe generar recurso con contenido no vacío")
    void testExportCostCentersResourceNotEmpty() throws IOException {
        when(costCenterRepository.findAllByIdEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(List.of(entity));

        Resource result = service.exportCostCenters(ENTERPRISE_ID, true);

        assertNotNull(result);
        assertNotNull(result.getInputStream());
        assertTrue(result.contentLength() > 100);
    }
}
