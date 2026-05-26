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

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.audit.AuditAspectClassesDocuments;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services.IDocumentClassService;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuditAspectClassesDocumentTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;
    @Mock
    private AuditEventPublisher auditEventPublisher;
    @Mock
    private IDocumentClassService documentClassService;

    private TestableAuditAspectClassesDocuments aspect;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String NAME = "Facturas";

    private DocumentClass domainObj;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectClassesDocuments(auditEventBuilder, auditEventPublisher, documentClassService);
        domainObj = DocumentClass.builder()
                .id(ID).name(NAME).idEnterprise(ENTERPRISE_ID).status(true).build();
    }

    // EntityTomap
    @Test
    @DisplayName("entityToMap - DocumentClass válido debe mapear todos los campos")
    void entityToMap_documentClassValido() {
        Map<String, Object> map = aspect.testEntityToMap(domainObj);

        assertAll(
                () -> assertNotNull(map),
                () -> assertEquals(ID, map.get("id")),
                () -> assertEquals(ENTERPRISE_ID, map.get("entId")),
                () -> assertEquals(NAME, map.get("name")),
                () -> assertEquals(true, map.get("state")));
    }

    @Test
    @DisplayName("entityToMap - Objeto que no es DocumentClass debe retornar null")
    void entityToMap_objetoInvalido_retornaNull() {
        assertNull(aspect.testEntityToMap("no soy DocumentClass"));
        assertNull(aspect.testEntityToMap(42));
        assertNull(aspect.testEntityToMap(null));
    }

    @Test
    @DisplayName("entityToMap - Campos null en DocumentClass no deben aparecer en el mapa")
    void entityToMap_camposNull_noAparecen() {
        DocumentClass sinNombre = DocumentClass.builder()
                .id(ID).idEnterprise(ENTERPRISE_ID).status(true).build(); // name = null

        Map<String, Object> map = aspect.testEntityToMap(sinNombre);

        assertFalse(map.containsKey("name"), "Los campos null deben ser eliminados del mapa");
    }

    // FetchCurrentState
    @Test
    @DisplayName("fetchCurrentState - Con args[0] Long debe buscar por id y enterprise")
    void fetchCurrentState_argsConLong() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_CLASS");
        when(documentClassService.findById(ID, ENTERPRISE_ID)).thenReturn(domainObj);

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID });

        assertNotNull(state);
        assertEquals(NAME, state.get("name"));
        verify(documentClassService).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("fetchCurrentState - Con args[0] UpdateReq debe extraer id y enterprise del request")
    void fetchCurrentState_argsConUpdateReq() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_CLASS");
        DocumentClassUpdateReq req = new DocumentClassUpdateReq();
        req.setId(ID);
        req.setIdEnterprise(ENTERPRISE_ID);
        when(documentClassService.findById(ID, ENTERPRISE_ID)).thenReturn(domainObj);

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { req });

        assertNotNull(state);
        verify(documentClassService).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("fetchCurrentState - Cuando findById retorna null debe retornar null")
    void fetchCurrentState_servicioRetornaNull() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "DOCUMENT_CLASS");
        when(documentClassService.findById(ID, ENTERPRISE_ID)).thenReturn(null);

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID }));
    }

    @Test
    @DisplayName("fetchCurrentState - affectedTable desconocida debe retornar null")
    void fetchCurrentState_tablaDesconocida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TABLA_INEXISTENTE");

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, ENTERPRISE_ID }));
    }

    // ResolveEnterpriseId
    @Test
    @DisplayName("resolveEnterpriseId - CREATE extrae enterpriseId del result")
    void resolveEnterpriseId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_CLASS");
        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, domainObj, null);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE/INACTIVATE extrae de args[enterpriseIdArgIndex]")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "DOCUMENT_CLASS");
        when(auditable.enterpriseIdArgIndex()).thenReturn(1);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] { ID, ENTERPRISE_ID }, null, null);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE extrae de beforeData")
    void resolveEnterpriseId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_CLASS");
        Map<String, Object> before = Map.of("entId", ENTERPRISE_ID);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveEnterpriseId_update_sinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_CLASS");

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", entId);
    }

    // ResolveRegisterId
    @Test
    @DisplayName("resolveRegisterId - CREATE extrae id del result")
    void resolveRegisterId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "DOCUMENT_CLASS");
        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, domainObj, null);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - DELETE extrae de args[idArgIndex]")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "DOCUMENT_CLASS");
        when(auditable.idArgIndex()).thenReturn(0);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] { ID, ENTERPRISE_ID }, null, null);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE extrae de beforeData")
    void resolveRegisterId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_CLASS");
        Map<String, Object> before = Map.of("id", ID);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE sin beforeData retorna UNKNOWN")
    void resolveRegisterId_update_sinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "DOCUMENT_CLASS");
        String regId = aspect.testResolveRegisterId(
                auditable,
                new Object[] {},
                null,
                null);
        assertEquals("UNKNOWN", regId);
    }

    // BuildContext
    @Test
    @DisplayName("buildContext - Con beforeData con name debe incluir name en contexto")
    void buildContext_conNombre() {
        Map<String, Object> before = Map.of("name", NAME);
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);
        assertEquals(NAME, context.get("name"));
    }

    @Test
    @DisplayName("buildContext - beforeData null debe retornar mapa vacío")
    void buildContext_sinBeforeData() {
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, null);
        assertTrue(context.isEmpty());
    }

    // Helper
    private Auditable mockAuditable(OperationType type, String table) {
        Auditable a = mock(Auditable.class);
        lenient().when(a.operationType()).thenReturn(type);
        lenient().when(a.affectedTable()).thenReturn(table);
        lenient().when(a.moduleName()).thenReturn("CLASSES_OF_DOCUMENTS");
        lenient().when(a.idArgIndex()).thenReturn(0);
        lenient().when(a.enterpriseIdArgIndex()).thenReturn(1);
        return a;
    }

    private static class TestableAuditAspectClassesDocuments extends AuditAspectClassesDocuments {

        public TestableAuditAspectClassesDocuments(
                AuditEventBuilder builder,
                AuditEventPublisher publisher,
                IDocumentClassService service) {

            super(builder, publisher, service);
        }

        public Map<String, Object> testEntityToMap(Object o) {
            return super.entityToMap(o);
        }

        public Map<String, Object> testFetchCurrentState(
                Auditable a,
                Object[] args) {

            return super.fetchCurrentState(a, args);
        }

        public String testResolveEnterpriseId(
                Auditable a,
                Object[] args,
                Object result,
                Map<String, Object> before) {

            return super.resolveEnterpriseId(a, args, result, before);
        }

        public String testResolveRegisterId(
                Auditable a,
                Object[] args,
                Object result,
                Map<String, Object> before) {

            return super.resolveRegisterId(a, args, result, before);
        }

        public Map<String, Object> testBuildContext(
                Object[] args,
                Object result,
                Map<String, Object> before) {

            return super.buildContext(args, result, before);
        }
    }
}
