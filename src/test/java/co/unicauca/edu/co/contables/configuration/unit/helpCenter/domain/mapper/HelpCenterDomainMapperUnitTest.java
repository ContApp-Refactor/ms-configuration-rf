package co.unicauca.edu.co.contables.configuration.unit.helpCenter.domain.mapper;

import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.mapper.HelpCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.response.HelpCenterRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HelpCenterDomainMapperUnitTest {

    private HelpCenterDomainMapper mapper;

    private static final Long ID = 1L;
    private static final Integer MODULE_ID = 3;
    private static final String MODULE_NAME = "Comercial";
    private static final String NAME = "Guía de facturación";
    private static final String DESCRIPTION = "Descripción detallada de cómo realizar facturación";
    private static final Boolean STATUS = true;

    @BeforeEach
    void setUp() {
        mapper = new HelpCenterDomainMapper();
    }

    // ========== TO DOMAIN (CREATE REQ) TESTS ==========

    @Test
    @DisplayName("toDomain(CreateReq) - Debe mapear campos básicos correctamente")
    void testToDomainFromCreateReqMapsBasicFields() {
        // Arrange
        HelpCenterCreateReq request = HelpCenterCreateReq.builder()
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertEquals(MODULE_ID, domain.getModuleId());
        assertEquals(NAME, domain.getName());
        assertEquals(DESCRIPTION, domain.getDescription());
    }

    @Test
    @DisplayName("toDomain(CreateReq) - No debe asignar ID (se genera automáticamente)")
    void testToDomainFromCreateReqDoesNotAssignId() {
        // Arrange
        HelpCenterCreateReq request = HelpCenterCreateReq.builder()
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getId());
    }

    @Test
    @DisplayName("toDomain(CreateReq) - Debe mapear null correctamente")
    void testToDomainFromCreateReqWithNull() {
        // Arrange
        HelpCenterCreateReq request = null;

        // Act
        HelpCenter domain = mapper.toDomain(request);

        // Assert
        assertNull(domain);
    }

    @Test
    @DisplayName("toDomain(CreateReq) - Debe mapear todos los módulos")
    void testToDomainFromCreateReqMapsAllModules() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            HelpCenterCreateReq request = HelpCenterCreateReq.builder()
                    .moduleId(module.getId())
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();

            HelpCenter domain = mapper.toDomain(request);

            assertNotNull(domain);
            assertEquals(module.getId(), domain.getModuleId());
        }
    }

    // ========== TO DOMAIN (UPDATE REQ) TESTS ==========

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear campos básicos correctamente")
    void testToDomainFromUpdateReqMapsBasicFields() {
        // Arrange
        HelpCenterUpdateReq request = HelpCenterUpdateReq.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertEquals(ID, domain.getId());
        assertEquals(MODULE_ID, domain.getModuleId());
        assertEquals(NAME, domain.getName());
        assertEquals(DESCRIPTION, domain.getDescription());
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear ID correctamente (a diferencia de CreateReq)")
    void testToDomainFromUpdateReqMapsId() {
        // Arrange
        Long specificId = 42L;
        HelpCenterUpdateReq request = HelpCenterUpdateReq.builder()
                .id(specificId)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertEquals(specificId, domain.getId());
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear null correctamente")
    void testToDomainFromUpdateReqWithNull() {
        // Arrange
        HelpCenterUpdateReq request = null;

        // Act
        HelpCenter domain = mapper.toDomain(request);

        // Assert
        assertNull(domain);
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear todos los módulos")
    void testToDomainFromUpdateReqMapsAllModules() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            HelpCenterUpdateReq request = HelpCenterUpdateReq.builder()
                    .id(ID)
                    .moduleId(module.getId())
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();

            HelpCenter domain = mapper.toDomain(request);

            assertNotNull(domain);
            assertEquals(module.getId(), domain.getModuleId());
        }
    }

    // ========== TO RES TESTS ==========

    @Test
    @DisplayName("toRes - Debe mapear campos básicos correctamente")
    void testToResMapsBasicFields() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals(ID, response.getId());
        assertEquals(MODULE_ID, response.getModuleId());
        assertEquals(NAME, response.getName());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(STATUS, response.getStatus());
    }

    @Test
    @DisplayName("toRes - Debe resolver moduleName desde moduleId")
    void testToResResolvesModuleName() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals(MODULE_NAME, response.getModuleName());
    }

    @Test
    @DisplayName("toRes - Debe resolver moduleName para todos los módulos")
    void testToResResolvesModuleNameForAllModules() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            HelpCenter domain = HelpCenter.builder()
                    .id(ID)
                    .moduleId(module.getId())
                    .name(NAME)
                    .description(DESCRIPTION)
                    .status(STATUS)
                    .build();

            HelpCenterRes response = mapper.toRes(domain);

            assertNotNull(response);
            assertEquals(module.getName(), response.getModuleName());
        }
    }

    @Test
    @DisplayName("toRes - Debe mapear moduleName como null cuando moduleId es null")
    void testToResWithNullModuleId() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(null)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertNull(response.getModuleName());
    }

    @Test
    @DisplayName("toRes - Debe mapear moduleName como 'Módulo desconocido' cuando moduleId es inválido")
    void testToResWithInvalidModuleId() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(999)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals("Módulo desconocido", response.getModuleName());
    }

    @Test
    @DisplayName("toRes - Debe mapear null correctamente")
    void testToResWithNull() {
        // Arrange
        HelpCenter domain = null;

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNull(response);
    }

    @Test
    @DisplayName("toRes - Debe mapear status false correctamente")
    void testToResWithStatusFalse() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(false)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertFalse(response.getStatus());
    }

    @Test
    @DisplayName("toRes - Debe mapear módulo INVENTARIO_PROMEDIO_PONDERADO correctamente")
    void testToResMapsInventarioPromedioPonderado() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(1)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals("Inventario promedio ponderado", response.getModuleName());
    }

    @Test
    @DisplayName("toRes - Debe mapear módulo CONFIGURACION correctamente")
    void testToResMapsConfiguracion() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(9)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals("Configuración", response.getModuleName());
    }

    @Test
    @DisplayName("toRes - Debe mapear módulo TESORERIA con tilde correctamente")
    void testToResMapsTesoreriaWithAccent() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(4)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals("Tesorería", response.getModuleName());
    }
}
