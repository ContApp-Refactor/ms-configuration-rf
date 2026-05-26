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

import java.util.LinkedHashMap;
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
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.audit.AuditAspectTypesDocuments;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeService;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;

@ExtendWith(MockitoExtension.class)
class AuditAspectTypesDocumentsTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;
    @Mock
    private AuditEventPublisher auditEventPublisher;
    @Mock
    private IDocumentTypeService documentTypeService;

    private TestableAuditAspectTypesDocuments aspect;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String NAME = "Factura Electrónica";
    private static final String PREFIX = "FAC";
    private static final Long DOCUMENT_CLASS_ID = 10L;
    private static final Integer MODULE_ID = 20;

    private DocumentType domainObj;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectTypesDocuments(auditEventBuilder, auditEventPublisher, documentTypeService);
        domainObj = DocumentType.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .name(NAME)
                .prefix(PREFIX)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .status(true)
                .build();
    }

    // entityToMap

    @Test
    @DisplayName("entityToMap - DocumentType válido debe mapear todos los campos")
    void entityToMap_documentTypeValido() {
        Map<String, Object> map = aspect.testEntityToMap(domainObj);

        assertAll(
                () -> assertNotNull(map),
                () -> assertEquals(ID, map.get("id")),
                () -> assertEquals(ENTERPRISE_ID, map.get("entId")),
                () -> assertEquals(NAME, map.get("name")),
                () -> assertEquals(PREFIX, map.get("prefix")),
                () -> assertEquals(DOCUMENT_CLASS_ID, map.get("documentClassId")),
                () -> assertEquals(MODULE_ID, map.get("moduleId")),
                () -> assertEquals(true, map.get("state")));
    }

    @Test
    @DisplayName("entityToMap - Campos null no deben aparecer")
    void entityToMap_camposNull() {
        DocumentType sinPrefix = DocumentType.builder()
                .id(ID).idEnterprise(ENTERPRISE_ID).name(NAME).status(true).build();

        Map<String, Object> map = aspect.testEntityToMap(sinPrefix);

        assertFalse(map.containsKey("prefix"));
    }

    @Test
    @DisplayName("entityToMap - Objeto inválido retorna null")
    void entityToMap_objetoInvalido() {
        assertNull(aspect.testEntityToMap("no es DocumentType"));
        assertNull(aspect.testEntityToMap(null));
    }

    // fetchCurrentState
    @Test
    @DisplayName("fetchCurrentState - Con args[0] Long debe buscar por id y enterprise")
    void fetchCurrentState_argsConLong() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_TYPE");
        when(documentTypeService.findById(ID, ENTERPRISE_ID)).thenReturn(domainObj);

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID });

        assertNotNull(state);
        assertEquals(NAME, state.get("name"));
        verify(documentTypeService).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("fetchCurrentState - Con args[0] UpdateReq debe extraer id y enterprise del request")
    void fetchCurrentState_argsConUpdateReq() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_TYPE");
        DocumentTypeUpdateReq req = new DocumentTypeUpdateReq();
        req.setId(ID);
        req.setIdEnterprise(ENTERPRISE_ID);
        when(documentTypeService.findById(ID, ENTERPRISE_ID)).thenReturn(domainObj);

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { req });

        assertNotNull(state);
        verify(documentTypeService).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("fetchCurrentState - Service retorna null debe retornar null")
    void fetchCurrentState_serviceRetornaNull() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "DOCUMENT_TYPE");
        when(documentTypeService.findById(ID, ENTERPRISE_ID)).thenReturn(null);

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID }));
    }

    @Test
    @DisplayName("fetchCurrentState - affectedTable desconocida retorna null")
    void fetchCurrentState_tablaDesconocida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TABLA_INEXISTENTE");

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID }));
    }

    // resolveEnterpriseId
    @Test
    @DisplayName("resolveEnterpriseId - CREATE extrae del result")
    void resolveEnterpriseId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_TYPE");
        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, domainObj, null);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - CREATE con result null retorna UNKNOWN")
    void resolveEnterpriseId_create_resultNull() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_TYPE");
        String entId = aspect.testResolveEnterpriseId(
                auditable,
                new Object[] {},
                null,
                null);
        assertEquals("UNKNOWN", entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - CREATE con result inválido retorna UNKNOWN")
    void resolveEnterpriseId_create_resultInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_TYPE");
        String entId = aspect.testResolveEnterpriseId(
                auditable,
                new Object[] {},
                "NO_ES_DOCUMENT_TYPE",
                null);
        assertEquals("UNKNOWN", entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE/INACTIVATE extrae de args por índice")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "DOCUMENT_TYPE");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] { ID, ENTERPRISE_ID }, null, null);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE extrae de beforeData")
    void resolveEnterpriseId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_TYPE");
        Map<String, Object> before = Map.of("entId", ENTERPRISE_ID);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveEnterpriseId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_TYPE");

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", entId);
    }

    // resolveRegisterId
    @Test
    @DisplayName("resolveRegisterId - CREATE extrae id del result")
    void resolveRegisterId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_TYPE");
        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, domainObj, null);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - CREATE con result inválido retorna UNKNOWN")
    void resolveRegisterId_create_resultInvalido() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_TYPE");
        String regId = aspect.testResolveRegisterId(
                auditable,
                new Object[] {},
                "NO_ES_DOCUMENT_TYPE",
                null);
        assertEquals("UNKNOWN", regId);
    }

    @Test
    @DisplayName("resolveRegisterId - CREATE con result null retorna UNKNOWN")
    void resolveRegisterId_create_resultNull() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_TYPE");
        String regId = aspect.testResolveRegisterId(
                auditable,
                new Object[] {},
                null,
                null);
        assertEquals("UNKNOWN", regId);
    }

    @Test
    @DisplayName("resolveRegisterId - DELETE extrae de args por índice")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "DOCUMENT_TYPE");
        when(auditable.idArgIndex()).thenReturn(0);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] { ID, ENTERPRISE_ID }, null, null);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE extrae de beforeData")
    void resolveRegisterId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_TYPE");
        Map<String, Object> before = Map.of("id", ID);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveRegisterId_updateSinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_TYPE");
        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", regId);
    }

    // buildContext
    @Test
    @DisplayName("buildContext - Debe incluir prefix y name si existen")
    void buildContext_conPrefixYName() {
        Map<String, Object> before = Map.of("prefix", PREFIX, "name", NAME);
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);

        assertAll(
                () -> assertEquals(PREFIX, context.get("prefix")),
                () -> assertEquals(NAME, context.get("name")));
    }

    @Test
    @DisplayName("buildContext - Solo prefix presente debe incluir solo prefix")
    void buildContext_soloPrefix() {
        Map<String, Object> before = Map.of("prefix", PREFIX);
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);

        assertEquals(PREFIX, context.get("prefix"));
        assertFalse(context.containsKey("name"));
    }

    @Test
    @DisplayName("buildContext - beforeData null retorna mapa vacío")
    void buildContext_beforeDataNull() {
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, null);
        assertTrue(context.isEmpty());
    }

    @Test
    @DisplayName("buildContext - beforeData con valores null no debe incluir campos")
    void buildContext_conNullValues() {
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("prefix", null);
        before.put("name", null);
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);
        assertTrue(context.isEmpty());
    }

    // Helper
    private Auditable mockAuditable(OperationType type, String table) {
        Auditable a = mock(Auditable.class);
        lenient().when(a.operationType()).thenReturn(type);
        lenient().when(a.affectedTable()).thenReturn(table);
        lenient().when(a.moduleName()).thenReturn("TYPE_OF_DOCUMENTS");
        lenient().when(a.idArgIndex()).thenReturn(0);
        lenient().when(a.enterpriseIdArgIndex()).thenReturn(1);
        return a;
    }

    private static class TestableAuditAspectTypesDocuments extends AuditAspectTypesDocuments {
        public TestableAuditAspectTypesDocuments(AuditEventBuilder builder, AuditEventPublisher publisher,
                IDocumentTypeService service) {
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
