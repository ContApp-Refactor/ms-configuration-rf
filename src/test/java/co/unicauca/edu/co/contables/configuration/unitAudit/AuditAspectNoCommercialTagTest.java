package co.unicauca.edu.co.contables.configuration.unitAudit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

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
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.audit.AuditAspectNoCommerTag;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.models.Tag;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services.ITagService;

@ExtendWith(MockitoExtension.class)
class AuditAspectNoCommercialTagTest {

    @Mock
    private AuditEventBuilder auditEventBuilder;
    @Mock
    private AuditEventPublisher auditEventPublisher;
    @Mock
    private ITagService tagService;

    private TestableAuditAspectNoCommerTag aspect;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String TITLE = "Etiqueta Comercial";
    private static final String DESCRIPTION = "Descripción de la etiqueta";

    private Tag domainObj;

    @BeforeEach
    void setUp() {
        aspect = new TestableAuditAspectNoCommerTag(auditEventBuilder, auditEventPublisher, tagService);
        domainObj = Tag.builder()
                .id(ID)
                .enterpriseId(ENTERPRISE_ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();
    }

    // entityToMap

    @Test
    @DisplayName("entityToMap - Tag válido debe mapear todos los campos")
    void entityToMap_tagValido() {
        Map<String, Object> map = aspect.testEntityToMap(domainObj);

        assertAll(
                () -> assertNotNull(map),
                () -> assertEquals(ID, map.get("id")),
                () -> assertEquals(ENTERPRISE_ID, map.get("entId")),
                () -> assertEquals(TITLE, map.get("title")),
                () -> assertEquals(DESCRIPTION, map.get("description")));
    }

    @Test
    @DisplayName("entityToMap - Campos null no deben aparecer")
    void entityToMap_camposNull() {
        Tag sinDescripcion = Tag.builder()
                .id(ID).enterpriseId(ENTERPRISE_ID).title(TITLE).build();

        Map<String, Object> map = aspect.testEntityToMap(sinDescripcion);

        assertFalse(map.containsKey("description"));
    }

    @Test
    @DisplayName("entityToMap - Objeto inválido retorna null")
    void entityToMap_objetoInvalido() {
        assertNull(aspect.testEntityToMap("no es Tag"));
        assertNull(aspect.testEntityToMap(null));
    }

    // fetchCurrentState

    @Test
    @DisplayName("fetchCurrentState - UPDATE: debe buscar por id y enterpriseId del tag")
    void fetchCurrentState_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "NO_COMMERCIAL_TAG");
        Tag tagArg = Tag.builder().enterpriseId(ENTERPRISE_ID).build();
        when(tagService.getTag(ID, ENTERPRISE_ID)).thenReturn(Optional.of(domainObj));

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { ID, tagArg });

        assertNotNull(state);
        assertEquals(TITLE, state.get("title"));
        verify(tagService).getTag(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("fetchCurrentState - DELETE: solo recibe id, busca con getTagById")
    void fetchCurrentState_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "NO_COMMERCIAL_TAG");
        when(tagService.getTagById(ID)).thenReturn(Optional.of(domainObj));

        Map<String, Object> state = aspect.testFetchCurrentState(auditable, new Object[] { ID });

        assertNotNull(state);
        verify(tagService).getTagById(ID);
        verify(tagService, never()).getTag(anyLong(), anyString());
    }

    @Test
    @DisplayName("fetchCurrentState - UPDATE: service retorna empty debe retornar null")
    void fetchCurrentState_updateServiceRetornaEmpty() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "NO_COMMERCIAL_TAG");
        Tag tagArg = Tag.builder().enterpriseId(ENTERPRISE_ID).build();
        when(tagService.getTag(ID, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, tagArg }));
    }

    @Test
    @DisplayName("fetchCurrentState - DELETE: service retorna empty debe retornar null")
    void fetchCurrentState_deleteServiceRetornaEmpty() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "NO_COMMERCIAL_TAG");
        when(tagService.getTagById(ID)).thenReturn(Optional.empty());

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID }));
    }

    @Test
    @DisplayName("fetchCurrentState - affectedTable desconocida retorna null")
    void fetchCurrentState_tablaDesconocida() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "TABLA_INEXISTENTE");

        assertNull(aspect.testFetchCurrentState(auditable, new Object[] { ID, new Tag() }));
    }

    // resolveEnterpriseId

    @Test
    @DisplayName("resolveEnterpriseId - CREATE extrae del result")
    void resolveEnterpriseId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "NO_COMMERCIAL_TAG");
        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, domainObj, null);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - UPDATE extrae de beforeData")
    void resolveEnterpriseId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "NO_COMMERCIAL_TAG");
        Map<String, Object> before = Map.of("entId", ENTERPRISE_ID);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - DELETE extrae de beforeData")
    void resolveEnterpriseId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "NO_COMMERCIAL_TAG");
        Map<String, Object> before = Map.of("entId", ENTERPRISE_ID);

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, before);
        assertEquals(ENTERPRISE_ID, entId);
    }

    @Test
    @DisplayName("resolveEnterpriseId - Sin beforeData retorna UNKNOWN")
    void resolveEnterpriseId_sinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "NO_COMMERCIAL_TAG");

        String entId = aspect.testResolveEnterpriseId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", entId);
    }

    // resolveRegisterId
    @Test
    @DisplayName("resolveRegisterId - CREATE extrae id del result")
    void resolveRegisterId_create() {
        Auditable auditable = mockAuditable(OperationType.CREATE, "NO_COMMERCIAL_TAG");
        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, domainObj, null);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - UPDATE extrae de beforeData")
    void resolveRegisterId_update() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "NO_COMMERCIAL_TAG");
        Map<String, Object> before = Map.of("id", ID);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - DELETE extrae de beforeData")
    void resolveRegisterId_delete() {
        Auditable auditable = mockAuditable(OperationType.DELETE, "NO_COMMERCIAL_TAG");
        Map<String, Object> before = Map.of("id", ID);

        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, before);
        assertEquals(String.valueOf(ID), regId);
    }

    @Test
    @DisplayName("resolveRegisterId - Sin beforeData retorna UNKNOWN")
    void resolveRegisterId_sinBeforeData() {
        Auditable auditable = mockAuditable(OperationType.UPDATE, "NO_COMMERCIAL_TAG");
        String regId = aspect.testResolveRegisterId(auditable, new Object[] {}, null, null);
        assertEquals("UNKNOWN", regId);
    }

    // Buildcontext
    @Test
    @DisplayName("buildContext - Debe incluir title si existe")
    void buildContext_conTitle() {
        Map<String, Object> before = Map.of("title", TITLE);
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);

        assertEquals(TITLE, context.get("title"));
    }

    @Test
    @DisplayName("buildContext - beforeData null retorna mapa vacío")
    void buildContext_beforeDataNull() {
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, null);
        assertTrue(context.isEmpty());
    }

    @Test
    @DisplayName("buildContext - Si no hay title retorna vacío")
    void buildContext_sinTitle() {
        Map<String, Object> before = Map.of("otherField", "value");
        Map<String, Object> context = aspect.testBuildContext(new Object[] {}, null, before);

        assertTrue(context.isEmpty());
    }

    // Helper
    private Auditable mockAuditable(OperationType type, String table) {
        Auditable a = mock(Auditable.class);
        lenient().when(a.operationType()).thenReturn(type);
        lenient().when(a.affectedTable()).thenReturn(table);
        lenient().when(a.moduleName()).thenReturn("NO_COMMERCIAL_TAGS");
        lenient().when(a.idArgIndex()).thenReturn(0);
        lenient().when(a.enterpriseIdArgIndex()).thenReturn(1);
        return a;
    }

    private static class TestableAuditAspectNoCommerTag extends AuditAspectNoCommerTag {
        public TestableAuditAspectNoCommerTag(AuditEventBuilder builder, AuditEventPublisher publisher,
                ITagService service) {
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
