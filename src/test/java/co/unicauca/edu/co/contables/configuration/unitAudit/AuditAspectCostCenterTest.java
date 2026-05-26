package co.unicauca.edu.co.contables.configuration.unitAudit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.audit.AuditAspectCostCenters;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;

@ExtendWith(MockitoExtension.class)
public class AuditAspectCostCenterTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;
    @Mock
    private AuditEventPublisher auditEventPublisher;
    @Mock
    private ICostCenterService costCenterService;

    private TestableAuditAspectCostCenters aspect;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String CODE = "CC-001";
    private static final String NAME = "Centro de Costos Principal";

    private CostCenter domainObj;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectCostCenters(auditEventBuilder, auditEventPublisher, costCenterService);
        domainObj = CostCenter.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .code(CODE)
                .name(NAME)
                .status(true)
                .build();
    }

    // EntityTomap
    @Test
    @DisplayName("entityToMap - CostCenter válido debe mapear todos los campos")
    void entityToMap_costCenterValido() {
        Map<String, Object> map = aspect.testEntityToMap(domainObj);

        assertAll(
                () -> assertNotNull(map),
                () -> assertEquals(ID, map.get("id")),
                () -> assertEquals(ENTERPRISE_ID, map.get("entId")),
                () -> assertEquals(CODE, map.get("code")),
                () -> assertEquals(NAME, map.get("name")),
                () -> assertEquals(true, map.get("state")),
                () -> assertNull(map.get("parentId")));
    }

    @Test
    @DisplayName("entityToMap - Con parentId debe mapear parentId")
    void entityToMap_conParentId() {
        CostCenter parent = CostCenter.builder().id(99L).build();
        domainObj.setParent(parent);

        Map<String, Object> map = aspect.testEntityToMap(domainObj);

        assertEquals(99L, map.get("parentId"));
    }

    @Test
    @DisplayName("entityToMap - Objeto inválido retorna null")
    void entityToMap_objetoInvalido() {
        assertNull(aspect.testEntityToMap("no es CostCenter"));
        assertNull(aspect.testEntityToMap(null));
    }

    @Test
    @DisplayName("entityToMap - Campos null no deben aparecer en el mapa")
    void entityToMap_camposNull() {
        CostCenter sinCode = CostCenter.builder()
                .id(ID).idEnterprise(ENTERPRISE_ID).name(NAME).status(true).build();

        Map<String, Object> map = aspect.testEntityToMap(sinCode);

        assertFalse(map.containsKey("code"));
    }

    // FetchCurrentState
    @Test
    @DisplayName("fetchCurrentState - Con args[0] Long debe buscar por id y enterprise")
    void fetchCurrentState_argsConLong() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "COST_CENTER");
        when(costCenterService.findById(ID, ENTERPRISE_ID)).thenReturn(domainObj);

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID });

        assertNotNull(state);
        assertEquals(NAME, state.get("name"));
        verify(costCenterService).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("fetchCurrentState - Con args[0] UpdateReq debe extraer id y enterprise del request")
    void fetchCurrentState_argsConUpdateReq() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "COST_CENTER");
        CostCenterUpdateReq req = new CostCenterUpdateReq();
        req.setId(ID);
        req.setIdEnterprise(ENTERPRISE_ID);
        when(costCenterService.findById(ID, ENTERPRISE_ID)).thenReturn(domainObj);

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { req });

        assertNotNull(state);
        verify(costCenterService).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("fetchCurrentState - Service retorna null debe retornar null")
    void fetchCurrentState_serviceRetornaNull() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "COST_CENTER");
        when(costCenterService.findById(ID, ENTERPRISE_ID)).thenReturn(null);

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID }));
    }

    @Test
    @DisplayName("fetchCurrentState - affectedTable desconocida retorna null")
    void fetchCurrentState_tablaDesconocida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TABLA_INEXISTENTE");

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID }));
    }

    // ResolveEnterpriseid
    @Test
    @DisplayName("resolveEnterpriseId - CREATE extrae del result")
    void resolveEnterpriseId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "COST_CENTER");
        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, domainObj, null);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE/INACTIVATE extrae de args por índice")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "COST_CENTER");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] { ID, ENTERPRISE_ID }, null, null);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE extrae de beforeData")
    void resolveEnterpriseId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "COST_CENTER");
        Map<String, Object> before = Map.of("entId", ENTERPRISE_ID);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveEnterpriseId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "COST_CENTER");

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", entId);
    }

    // Resolveregisterid
    @Test
    @DisplayName("resolveRegisterId - CREATE extrae id del result")
    void resolveRegisterId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "COST_CENTER");
        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, domainObj, null);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - DELETE extrae de args por índice")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "COST_CENTER");
        when(auditable.idArgIndex()).thenReturn(0);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] { ID, ENTERPRISE_ID }, null, null);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE extrae de beforeData")
    void resolveRegisterId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "COST_CENTER");
        Map<String, Object> before = Map.of("id", ID);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveRegisterId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "COST_CENTER");
        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", regId);
    }

    // buildContext

    @Test
    @DisplayName("buildContext - Debe incluir code y name si existen")
    void buildContext_conCodeYName() {
        Map<String, Object> before = Map.of("code", CODE, "name", NAME);
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);

        assertAll(
                () -> assertEquals(CODE, context.get("code")),
                () -> assertEquals(NAME, context.get("name")));
    }

    @Test
    @DisplayName("buildContext - Solo code presente debe incluir solo code")
    void buildContext_soloCode() {
        Map<String, Object> before = Map.of("code", CODE);
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);

        assertEquals(CODE, context.get("code"));
        assertFalse(context.containsKey("name"));
    }

    @Test
    @DisplayName("buildContext - beforeData null retorna mapa vacío")
    void buildContext_beforeDataNull() {
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, null);
        assertTrue(context.isEmpty());
    }

    // Helper
    private Auditable mockAuditable(OperationType type, String table) {
        Auditable a = mock(Auditable.class);
        lenient().when(a.operationType()).thenReturn(type);
        lenient().when(a.affectedTable()).thenReturn(table);
        lenient().when(a.moduleName()).thenReturn("COST_CENTERS");
        lenient().when(a.idArgIndex()).thenReturn(0);
        lenient().when(a.enterpriseIdArgIndex()).thenReturn(1);
        return a;
    }

    private static class TestableAuditAspectCostCenters extends AuditAspectCostCenters {
        public TestableAuditAspectCostCenters(AuditEventBuilder builder, AuditEventPublisher publisher,
                ICostCenterService service) {
            super(builder, publisher, service);
        }

        public Map<String, Object> testEntityToMap(Object o) {
            return super.entityToMap(o);
        }

        public Map<String, Object> testFetchCurrentState(Auditable a, Object[] args) {
            return super.fetchCurrentState(a, args);
        }

        public String testResolveEnterpriseId(Auditable a, Object[] args, Object result, Map<String, Object> before) {
            return super.resolveEnterpriseId(a, args, result, before);
        }

        public String testResolveRegisterId(Auditable a, Object[] args, Object result, Map<String, Object> before) {
            return super.resolveRegisterId(a, args, result, before);
        }

        public Map<String, Object> testBuildContext(Object[] args, Object result, Map<String, Object> before) {
            return super.buildContext(args, result, before);
        }
    }
}
