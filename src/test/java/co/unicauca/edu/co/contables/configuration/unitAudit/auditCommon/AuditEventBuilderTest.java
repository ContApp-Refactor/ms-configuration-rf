package co.unicauca.edu.co.contables.configuration.unitAudit.auditCommon;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.configuration.commons.security.IJwtUtils;

@ExtendWith(MockitoExtension.class)
class AuditEventBuilderTest {

    @Mock
    private IJwtUtils jwtUtils;
    @InjectMocks
    private AuditEventBuilder builder;

    private static final String USER_ID = "user-123";
    private static final String USERNAME = "jdoe";
    private static final String ENTERPRISE = "ENT-001";
    private static final String REGISTER_ID = "42";

    @BeforeEach
    void setUp() {
        when(jwtUtils.getId()).thenReturn(USER_ID);
        when(jwtUtils.getUsername()).thenReturn(USERNAME);
        when(jwtUtils.getRealmRoles()).thenReturn(List.of("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("build - Debe construir DTO con todos los campos correctamente")
    void build_camposCorrectos() {
        Auditable auditable = mockAuditable("DOCUMENT_CLASS", "CLASSES_OF_DOCUMENTS");
        Map<String, Object> dataObject = Map.of("entity", Map.of("id", 1L));

        OperationEventDto dto = builder.build(auditable, OperationType.CREATE, ENTERPRISE, REGISTER_ID, dataObject);

        assertAll(
                () -> assertEquals(ENTERPRISE, dto.getEnterpriseId()),
                () -> assertEquals(USER_ID, dto.getUserId()),
                () -> assertEquals(USERNAME, dto.getUserName()),
                () -> assertEquals(List.of("ROLE_ADMIN"), dto.getUserRole()),
                () -> assertEquals("CREATE", dto.getOperationType()),
                () -> assertEquals("DOCUMENT_CLASS", dto.getAffectedTable()),
                () -> assertEquals("CLASSES_OF_DOCUMENTS", dto.getModuleName()),
                () -> assertEquals(REGISTER_ID, dto.getRegisterId()),
                () -> assertEquals(dataObject, dto.getDataObject()),
                () -> assertNotNull(dto.getOperationAt()));
    }

    @Test
    @DisplayName("build - operationType resuelto debe usarse, no el de la anotación")
    void build_usaResolvedOperationType() {
        // La anotación dice INACTIVATE pero el tipo resuelto es ACTIVATE
        Auditable auditable = mockAuditable("DOCUMENT_CLASS", "CLASSES_OF_DOCUMENTS");

        OperationEventDto dto = builder.build(auditable, OperationType.ACTIVATE, ENTERPRISE, REGISTER_ID, Map.of());

        assertEquals("ACTIVATE", dto.getOperationType());
    }

    @Test
    @DisplayName("build - operationAt debe ser un instante reciente")
    void build_operationAt_esReciente() {
        Auditable auditable = mockAuditable("DOCUMENT_CLASS", "CLASSES_OF_DOCUMENTS");

        OperationEventDto dto = builder.build(auditable, OperationType.DELETE, ENTERPRISE, REGISTER_ID, Map.of());

        long diffMs = Math.abs(System.currentTimeMillis() - dto.getOperationAt().toEpochMilli());
        assertTrue(diffMs < 2000, "operationAt debe estar dentro de los últimos 2 segundos");
    }

    @Test
    @DisplayName("build - No debe explotar si JWT retorna valores null")
    void build_jwtNull_noExplota() {
        when(jwtUtils.getId()).thenReturn(null);
        when(jwtUtils.getUsername()).thenReturn(null);
        when(jwtUtils.getRealmRoles()).thenReturn(null);
        Auditable auditable = mockAuditable(
                "DOCUMENT_CLASS",
                "CLASSES_OF_DOCUMENTS");
        Map<String, Object> dataObject = Map.of(
                "entity", Map.of("id", 1L));
        OperationEventDto dto = builder.build(
                auditable,
                OperationType.CREATE,
                ENTERPRISE,
                REGISTER_ID,
                dataObject);
        assertAll(
                () -> assertNull(dto.getUserId()),
                () -> assertNull(dto.getUserName()),
                () -> assertNull(dto.getUserRole()),
                () -> assertEquals(ENTERPRISE, dto.getEnterpriseId()),
                () -> assertEquals("CREATE", dto.getOperationType()),
                () -> assertNotNull(dto.getOperationAt()));
    }

    private Auditable mockAuditable(String table, String module) {
        Auditable a = mock(Auditable.class);
        when(a.affectedTable()).thenReturn(table);
        when(a.moduleName()).thenReturn(module);
        return a;
    }
}
